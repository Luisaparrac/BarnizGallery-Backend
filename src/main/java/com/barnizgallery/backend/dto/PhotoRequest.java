package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body to register a photo by URL, with its alternative text in both languages.
 */
public record PhotoRequest(
        @NotBlank @Size(max = 255) String fileUrl,
        @Size(max = 50) String angle,
        @Size(max = 300) String altEs,
        @Size(max = 300) String altEn) {
}
