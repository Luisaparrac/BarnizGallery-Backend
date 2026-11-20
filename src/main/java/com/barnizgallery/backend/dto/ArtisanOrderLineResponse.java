package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.barnizgallery.backend.enums.FulfillmentStatus;
import com.barnizgallery.backend.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * A paid order line of the artisan's store, with what the artisan needs to ship it and what they will receive.
 */
public record ArtisanOrderLineResponse(
        Long orderItemId,
        Long orderId,
        String orderCode,
        OrderStatus orderStatus,
        Instant paidAt,
        Long artworkId,
        String titleEs,
        String titleEn,
        int quantity,
        BigDecimal lineTotal,
        BigDecimal artisanNetAmount,
        String currency,
        FulfillmentStatus fulfillmentStatus,
        String carrier,
        String trackingNumber,
        @JsonRawValue String shippingAddress) {
}
