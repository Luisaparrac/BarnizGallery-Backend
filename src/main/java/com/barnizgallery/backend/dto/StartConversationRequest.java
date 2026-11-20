package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Starts (or continues) a conversation with a master, with its first message. At most one context: an artwork,
 * a custom request or an order. {@code subject} is accepted but not stored: the table has no column for it.
 */
public record StartConversationRequest(
        @NotNull Long masterId,
        @Size(max = 200) String subject,
        @NotBlank @Size(max = 4000) String content,
        Long artworkId,
        Long customRequestId,
        Long orderId) {
}
