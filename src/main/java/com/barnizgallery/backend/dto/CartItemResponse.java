package com.barnizgallery.backend.dto;

import java.math.BigDecimal;

/**
 * A line of the cart with the current price of the artwork. {@code available} is false when the artwork can no
 * longer be bought or its stock is lower than the quantity; such lines block the checkout.
 */
public record CartItemResponse(
        Long artworkId,
        Long storeId,
        String title,
        String titleEs,
        String titleEn,
        BigDecimal unitPrice,
        String currency,
        int quantity,
        BigDecimal lineTotal,
        Integer stock,
        boolean available,
        String coverUrl) {
}
