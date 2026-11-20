package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.JsonNode;

/**
 * Content of a store section. {@code sectionType} is a short code chosen by the frontend (for example
 * {@code hero}, {@code story}, {@code gallery}); {@code config} is an optional JSON object.
 */
public record StoreSectionRequest(
        @NotBlank @Pattern(regexp = "[a-z][a-z0-9_]{0,39}") String sectionType,
        @Size(max = 200) String titleEs,
        @Size(max = 200) String titleEn,
        @Size(max = 8000) String contentEs,
        @Size(max = 8000) String contentEn,
        @Size(max = 500) @Pattern(regexp = "https://\\S+") String imageUrl,
        JsonNode config) {
}
