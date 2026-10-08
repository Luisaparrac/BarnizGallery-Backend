package com.barnizgallery.backend.patterns.observer;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.model.enums.InteractionAction;
import com.barnizgallery.backend.service.ArtworkService;
import com.barnizgallery.backend.service.InteractionService;
import com.barnizgallery.backend.service.VisitorService;

/**
 * <b>Observer pattern – ConcreteObserver.</b>
 * <p>
 * Records a BID interaction ("ofertar") every time a bid is placed, using the
 * Factory Method creators. Bids are the strongest interest signal for recommendations.
 */
@Component
public class BidInteractionRecorder implements AuctionObserver {

    private final InteractionService interactionService;
    private final VisitorService visitorService;
    private final ArtworkService artworkService;

    public BidInteractionRecorder(InteractionService interactionService, VisitorService visitorService,
            ArtworkService artworkService) {
        this.interactionService = interactionService;
        this.visitorService = visitorService;
        this.artworkService = artworkService;
    }

    @Override
    public void onEvent(AuctionEvent event) {
        if (event.type() != AuctionEvent.Type.BID_PLACED) {
            return;
        }
        interactionService.save(visitorService.getVisitor(event.bid().visitorId()),
                artworkService.getArtwork(event.artworkId()), InteractionAction.BID, null);
    }
}
