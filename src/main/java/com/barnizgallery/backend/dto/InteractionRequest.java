package com.barnizgallery.backend.dto;

import com.barnizgallery.backend.enums.InteractionAction;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Body to record an interaction of a user with an artwork ({@code artworkId}) or, for store-level actions such as
 * {@code STORE_VISIT} and {@code CUSTOM_ORDER}, with a store ({@code storeId}).
 */
public record InteractionRequest(
        @NotNull Long userId,
        Long artworkId,
        Long storeId,
        @NotNull InteractionAction action,
        @Min(0) Integer durationSeconds) {
}
