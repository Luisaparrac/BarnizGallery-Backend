package com.barnizgallery.backend.patterns.observer;

import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

/**
 * <b>Observer pattern – ConcreteObserver.</b>
 * <p>
 * Sends every auction event live to {@code /topic/auctions/{auctionId}} through STOMP,
 * so everyone watching the auction in the 3D gallery sees new bids instantly.
 */
@Component
public class LiveAuctionBroadcaster implements AuctionObserver {

    private final SimpMessageSendingOperations messaging;

    public LiveAuctionBroadcaster(SimpMessageSendingOperations messaging) {
        this.messaging = messaging;
    }

    @Override
    public void onEvent(AuctionEvent event) {
        messaging.convertAndSend("/topic/auctions/" + event.auctionId(), event);
    }
}
