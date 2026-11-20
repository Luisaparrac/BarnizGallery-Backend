package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.Size;

/**
 * Changes of a photo; null fields stay as they are. {@code primary = true} makes it the artwork's main photo.
 */
public record PhotoUpdateRequest(
        @Size(max = 300) String altEs,
        @Size(max = 300) String altEn,
        @Size(max = 50) String angle,
        Boolean primary) {
}
