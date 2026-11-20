package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Creates the logged-in artisan's store from a template (by code). Without {@code slug} one is made from the
 * Spanish name.
 */
public record CreateStoreRequest(
        @NotBlank @Size(max = 60) String templateCode,
        @NotBlank @Size(max = 150) String nameEs,
        @NotBlank @Size(max = 150) String nameEn,
        @Size(max = 4000) String descriptionEs,
        @Size(max = 4000) String descriptionEn,
        @Pattern(regexp = "[a-z0-9]+(-[a-z0-9]+)*") @Size(min = 3, max = 80) String slug,
        Boolean acceptsCustomOrders) {
}
