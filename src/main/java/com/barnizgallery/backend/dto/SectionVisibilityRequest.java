package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Shows or hides a store section.
 */
public record SectionVisibilityRequest(@NotNull Boolean visible) {
}
