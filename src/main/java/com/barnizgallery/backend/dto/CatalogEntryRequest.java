package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Creates or edits a catalog entry. {@code code} is only read when creating (it never changes, because requests
 * and the AI service use it); {@code hex} is for colors and {@code imageUrl} for avatars. A missing
 * {@code active} keeps the entry active.
 */
public record CatalogEntryRequest(
        @Pattern(regexp = "[a-z0-9][a-z0-9_-]{0,59}") String code,
        @NotBlank @Size(max = 100) String nameEs,
        @NotBlank @Size(max = 100) String nameEn,
        @PositiveOrZero Integer sortOrder,
        Boolean active,
        @Pattern(regexp = "#[0-9A-Fa-f]{6}") String hex,
        @Size(max = 500) @Pattern(regexp = "https://\\S+") String imageUrl) {
}
