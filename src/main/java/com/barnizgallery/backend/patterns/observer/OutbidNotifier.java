package com.barnizgallery.backend.patterns.observer;

import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import com.barnizgallery.backend.dto.UserNotification;

/**
 * <b>Observer pattern – ConcreteObserver.</b>
 * <p>
 * When a new bid beats someone else's highest bid, warns that user on
 * {@code /topic/users/{userId}/notifications}.
 */
@Component
public class OutbidNotifier implements AuctionObserver {

    private final SimpMessageSendingOperations messaging;

    public OutbidNotifier(SimpMessageSendingOperations messaging) {
        this.messaging = messaging;
    }

    @Override
    public void onEvent(AuctionEvent event) {
        if (event.type() != AuctionEventType.BID_PLACED || event.previousTopBidderId() == null) {
            return;
        }
        if (event.previousTopBidderId().equals(event.bid().userId())) {
            return; // the user raised their own bid
        }
        UserNotification notification = new UserNotification(UserNotification.OUTBID, event.auctionId(),
                event.artworkId(), "Your bid was outbid: the new highest bid is " + event.bid().amount() + " "
                        + event.bid().currency());
        messaging.convertAndSend("/topic/users/" + event.previousTopBidderId() + "/notifications",
                notification);
    }
}
