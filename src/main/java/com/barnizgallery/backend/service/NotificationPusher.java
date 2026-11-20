package com.barnizgallery.backend.service;

import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Pushes each stored notification to {@code /topic/users/{id}/notifications} once its transaction commits, so a
 * user is never told about a notification that was rolled back.
 */
@Component
public class NotificationPusher {

    private final SimpMessageSendingOperations messaging;

    public NotificationPusher(SimpMessageSendingOperations messaging) {
        this.messaging = messaging;
    }

    public static String topic(Long userId) {
        return "/topic/users/" + userId + "/notifications";
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void push(NotificationCreated event) {
        messaging.convertAndSend(topic(event.userId()), event.notification());
    }
}
