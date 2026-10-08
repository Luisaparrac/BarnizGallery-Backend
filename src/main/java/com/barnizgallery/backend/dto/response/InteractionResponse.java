package com.barnizgallery.backend.dto.response;

import java.time.LocalDateTime;

import com.barnizgallery.backend.model.enums.InteractionAction;

/**
 * A recorded interaction.
 */
public record InteractionResponse(
        Integer interactionId,
        Integer visitorId,
        Integer artworkId,
        InteractionAction action,
        Integer durationSeconds,
        LocalDateTime interactionDate) {
}
