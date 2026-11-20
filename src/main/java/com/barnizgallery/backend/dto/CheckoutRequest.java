package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Checkout of the whole cart to one of the user's saved addresses.
 */
public record CheckoutRequest(@NotNull Long addressId) {
}
