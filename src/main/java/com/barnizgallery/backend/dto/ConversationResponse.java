package com.barnizgallery.backend.dto;

import java.time.Instant;

/**
 * A conversation between a customer and a master, with its last message and the unread messages of the reader.
 * {@code subject} is always null and {@code status} always {@code OPEN}: the table stores neither.
 */
public record ConversationResponse(
        Long id,
        Long customerId,
        String customerName,
        Long masterId,
        String masterName,
        Long artworkId,
        Long customRequestId,
        Long orderId,
        String subject,
        String status,
        Instant lastMessageAt,
        String lastMessagePreview,
        long unreadCount) {
}
