package com.barnizgallery.backend.dto;

import java.time.Instant;

/**
 * A message of a conversation.
 */
public record MessageResponse(Long id, Long conversationId, Long senderId, String senderName, String content,
        Instant readAt, Instant createdAt) {
}
