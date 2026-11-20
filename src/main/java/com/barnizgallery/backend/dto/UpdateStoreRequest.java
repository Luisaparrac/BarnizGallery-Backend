package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.JsonNode;

/**
 * Changes of the artisan's store; null fields stay as they are. {@code theme} is a JSON object (colors, fonts...)
 * that replaces the current one.
 */
public record UpdateStoreRequest(
        @Size(min = 1, max = 150) String nameEs,
        @Size(min = 1, max = 150) String nameEn,
        @Size(max = 4000) String descriptionEs,
        @Size(max = 4000) String descriptionEn,
        @Pattern(regexp = "[a-z0-9]+(-[a-z0-9]+)*") @Size(min = 3, max = 80) String slug,
        @Size(max = 500) @Pattern(regexp = "https://\\S+") String logoUrl,
        @Size(max = 500) @Pattern(regexp = "https://\\S+") String bannerUrl,
        JsonNode theme,
        Boolean acceptsCustomOrders) {
}
