package com.barnizgallery.backend.dto;

import com.barnizgallery.backend.enums.ArtworkStatus;

import jakarta.validation.constraints.NotNull;

/**
 * New status of an artwork: {@code DRAFT}, {@code EXHIBITED} or {@code ARCHIVED}.
 */
public record ArtworkStatusRequest(@NotNull ArtworkStatus status) {
}
