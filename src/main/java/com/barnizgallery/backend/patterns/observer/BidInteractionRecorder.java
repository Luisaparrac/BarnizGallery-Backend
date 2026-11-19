package com.barnizgallery.backend.patterns.observer;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.enums.InteractionAction;
import com.barnizgallery.backend.service.ArtworkService;
import com.barnizgallery.backend.service.InteractionService;
import com.barnizgallery.backend.service.UserService;

/**
 * <b>Observer pattern – ConcreteObserver.</b>
 * <p>
 * Records a BID interaction ("ofertar") every time a bid is placed, using the
 * Factory Method creators. Bids are the strongest interest signal for recommendations.
 */
@Component
public class BidInteractionRecorder implements AuctionObserver {

    private final InteractionService interactionService;
    private final UserService userService;
    private final ArtworkService artworkService;

    public BidInteractionRecorder(InteractionService interactionService, UserService userService,
            ArtworkService artworkService) {
        this.interactionService = interactionService;
        this.userService = userService;
        this.artworkService = artworkService;
    }

    @Override
    public void onEvent(AuctionEvent event) {
        if (event.type() != AuctionEventType.BID_PLACED) {
            return;
        }
        interactionService.save(userService.getUser(event.bid().userId()),
                artworkService.getArtwork(event.artworkId()), InteractionAction.BID, null);
    }
}
