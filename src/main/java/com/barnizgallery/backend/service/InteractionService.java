package com.barnizgallery.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.InteractionRequest;
import com.barnizgallery.backend.dto.InteractionResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Interaction;
import com.barnizgallery.backend.entity.Visitor;
import com.barnizgallery.backend.enums.InteractionAction;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.mapper.InteractionMapper;
import com.barnizgallery.backend.patterns.factorymethod.InteractionFactory;
import com.barnizgallery.backend.patterns.factorymethod.InteractionFactoryProvider;
import com.barnizgallery.backend.repository.InteractionRepository;

/**
 * Records visitor interactions using the Factory Method pattern.
 */
@Service
public class InteractionService {

    private final InteractionRepository interactionRepository;
    private final InteractionFactoryProvider factoryProvider;
    private final VisitorService visitorService;
    private final ArtworkService artworkService;

    public InteractionService(InteractionRepository interactionRepository,
            InteractionFactoryProvider factoryProvider, VisitorService visitorService,
            ArtworkService artworkService) {
        this.interactionRepository = interactionRepository;
        this.factoryProvider = factoryProvider;
        this.visitorService = visitorService;
        this.artworkService = artworkService;
    }

    /**
     * Records an interaction sent by the frontend. BID interactions are not accepted here:
     * they are recorded automatically when a real bid is placed.
     */
    @Transactional
    public InteractionResponse record(InteractionRequest request) {
        if (request.action() == InteractionAction.BID) {
            throw BusinessRuleException.unprocessable(
                    "BID interactions are recorded automatically when a bid is placed");
        }
        Visitor visitor = visitorService.getVisitor(request.visitorId());
        Artwork artwork = artworkService.getArtwork(request.artworkId());
        return InteractionMapper.toResponse(save(visitor, artwork, request.action(), request.durationSeconds()));
    }

    /** Creates the interaction with the right factory and saves it. Also used by the bid observer. */
    @Transactional
    public Interaction save(Visitor visitor, Artwork artwork, InteractionAction action, Integer durationSeconds) {
        InteractionFactory factory = factoryProvider.forAction(action);
        Interaction interaction = factory.create(visitor, artwork, durationSeconds);
        return interactionRepository.save(interaction);
    }

    @Transactional(readOnly = true)
    public List<InteractionResponse> history(Integer visitorId) {
        visitorService.getVisitor(visitorId);
        return interactionRepository.findHistory(visitorId).stream()
                .map(InteractionMapper::toResponse)
                .toList();
    }
}
