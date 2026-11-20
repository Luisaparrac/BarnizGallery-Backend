package com.barnizgallery.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.InteractionRequest;
import com.barnizgallery.backend.dto.InteractionResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Interaction;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.InteractionAction;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.mapper.InteractionMapper;
import com.barnizgallery.backend.patterns.factorymethod.InteractionFactory;
import com.barnizgallery.backend.patterns.factorymethod.InteractionFactoryProvider;
import com.barnizgallery.backend.repository.InteractionRepository;

/**
 * Records user interactions using the Factory Method pattern.
 */
@Service
public class InteractionService {

    private final InteractionRepository interactionRepository;
    private final InteractionFactoryProvider factoryProvider;
    private final UserService userService;
    private final ArtworkService artworkService;

    public InteractionService(InteractionRepository interactionRepository,
            InteractionFactoryProvider factoryProvider, UserService userService,
            ArtworkService artworkService) {
        this.interactionRepository = interactionRepository;
        this.factoryProvider = factoryProvider;
        this.userService = userService;
        this.artworkService = artworkService;
    }

    /**
     * Records an interaction sent by the frontend. BID interactions are not accepted here:
     * they are recorded automatically when a real bid is placed.
     */
    @Transactional
    public InteractionResponse record(InteractionRequest request) {
        if (request.action() == InteractionAction.BID) {
            throw BusinessRuleException.unprocessable(ErrorCode.BID_INTERACTION_NOT_ALLOWED);
        }
        User user = userService.getUser(request.userId());
        Artwork artwork = artworkService.getArtwork(request.artworkId());
        return InteractionMapper.toResponse(save(user, artwork, request.action(), request.durationSeconds()));
    }

    /** Creates the interaction with the right factory and saves it. Also used by the bid observer. */
    @Transactional
    public Interaction save(User user, Artwork artwork, InteractionAction action, Integer durationSeconds) {
        InteractionFactory factory = factoryProvider.forAction(action);
        Interaction interaction = factory.create(user, artwork, durationSeconds);
        return interactionRepository.save(interaction);
    }

    @Transactional(readOnly = true)
    public List<InteractionResponse> history(Long userId) {
        userService.getUser(userId);
        return interactionRepository.findHistory(userId).stream()
                .map(InteractionMapper::toResponse)
                .toList();
    }
}
