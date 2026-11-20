package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Slug to give a store when it is published (required when the store has none yet).
 */
public record AdminPublishStoreRequest(@Pattern(regexp = "[a-z0-9]+(-[a-z0-9]+)*") @Size(min = 3, max = 80) String slug) {
}
