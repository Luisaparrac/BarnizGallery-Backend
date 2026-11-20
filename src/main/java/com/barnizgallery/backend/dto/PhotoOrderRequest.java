package com.barnizgallery.backend.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

/**
 * The new order of the photos of an artwork: every photo id, once, first to last.
 */
public record PhotoOrderRequest(@NotEmpty List<Long> photoIds) {
}
