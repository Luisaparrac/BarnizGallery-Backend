package com.barnizgallery.backend.dto;

import java.math.BigDecimal;

import com.barnizgallery.backend.enums.FulfillmentStatus;

/**
 * A line of an order as the buyer sees it.
 */
public record OrderItemResponse(
        Long orderItemId,
        Long storeId,
        Long artworkId,
        String titleEs,
        String titleEn,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        FulfillmentStatus fulfillmentStatus,
        String carrier,
        String trackingNumber,
        Long id,
        Long orderId,
        String storeName,
        Long auctionId,
        Long customOfferId,
        String description,
        BigDecimal artisanAmount) {
}
