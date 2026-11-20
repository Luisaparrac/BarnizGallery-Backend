package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.JsonNode;

/**
 * Creates or edits a store template. {@code code} is only read when creating. {@code defaultLayout} and
 * {@code defaultTheme} must be JSON objects; when missing they are {@code {}}.
 */
public record StoreTemplateRequest(
        @Pattern(regexp = "[a-z0-9][a-z0-9_-]{0,59}") String code,
        @NotBlank @Size(max = 100) String nameEs,
        @NotBlank @Size(max = 100) String nameEn,
        @Size(max = 1000) String descriptionEs,
        @Size(max = 1000) String descriptionEn,
        @Size(max = 500) @Pattern(regexp = "https://\\S+") String previewUrl,
        JsonNode defaultLayout,
        JsonNode defaultTheme,
        Boolean active) {
}
