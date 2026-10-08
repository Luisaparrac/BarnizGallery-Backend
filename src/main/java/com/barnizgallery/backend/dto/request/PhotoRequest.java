package com.barnizgallery.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body to register a photo by URL.
 */
public record PhotoRequest(
        @NotBlank @Size(max = 255) String fileUrl,
        @Size(max = 50) String angle) {
}
