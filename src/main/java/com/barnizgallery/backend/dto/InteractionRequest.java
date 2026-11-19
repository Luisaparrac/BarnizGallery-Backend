package com.barnizgallery.backend.dto;

import com.barnizgallery.backend.enums.InteractionAction;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Body to record an interaction of a visitor with an artwork.
 */
public record InteractionRequest(
        @NotNull Long visitorId,
        @NotNull Long artworkId,
        @NotNull InteractionAction action,
        @Min(0) Integer durationSeconds) {
}
