package com.barnizgallery.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Body to send a message to a master, optionally about one artwork.
 */
public record ContactMessageRequest(
        @NotNull Integer visitorId,
        @NotNull Integer masterId,
        Integer artworkId,
        @NotBlank String content) {
}
