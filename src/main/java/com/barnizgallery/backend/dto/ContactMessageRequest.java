package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Body to send a message to a master, optionally about one artwork.
 */
public record ContactMessageRequest(
        @NotNull Long userId,
        @NotNull Long masterId,
        Long artworkId,
        @NotBlank String content) {
}
