package com.barnizgallery.backend.dto;

import java.time.LocalDateTime;

import com.barnizgallery.backend.enums.InteractionAction;

/**
 * A recorded interaction.
 */
public record InteractionResponse(
        Long interactionId,
        Long userId,
        Long artworkId,
        Long storeId,
        InteractionAction action,
        Integer durationSeconds,
        LocalDateTime interactionDate) {
}
