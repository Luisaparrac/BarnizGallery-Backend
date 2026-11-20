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
    private final StoreService storeService;

    public InteractionService(InteractionRepository interactionRepository,
            InteractionFactoryProvider factoryProvider, UserService userService,
            ArtworkService artworkService, StoreService storeService) {
        this.interactionRepository = interactionRepository;
        this.factoryProvider = factoryProvider;
        this.userService = userService;
        this.artworkService = artworkService;
        this.storeService = storeService;
    }

    /**
     * Records an interaction sent by the frontend, with an artwork or (store-level actions) with a store. Bids and
     * purchases are not accepted here: the backend records them when they really happen.
     */
    @Transactional
    public InteractionResponse record(InteractionRequest request) {
        InteractionFactory factory = factoryProvider.forAction(request.action());
        if (factory.recordedByServer()) {
            throw BusinessRuleException.unprocessable(ErrorCode.INTERACTION_RECORDED_BY_SERVER,
                    request.action().getDbValue());
        }
        User user = userService.getUser(request.userId());
        Interaction interaction;
        if (factory.isStoreLevel()) {
            if (request.storeId() == null) {
                throw BusinessRuleException.unprocessable(ErrorCode.INTERACTION_TARGET_INVALID,
                        request.action().getDbValue());
            }
            interaction = factory.createForStore(user, storeService.getStore(request.storeId()),
                    request.durationSeconds());
        } else {
            if (request.artworkId() == null) {
                throw BusinessRuleException.unprocessable(ErrorCode.INTERACTION_TARGET_INVALID,
                        request.action().getDbValue());
            }
            interaction = factory.create(user, artworkService.getArtwork(request.artworkId()),
                    request.durationSeconds());
        }
        return InteractionMapper.toResponse(interactionRepository.save(interaction));
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
