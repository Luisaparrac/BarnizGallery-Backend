package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.barnizgallery.backend.enums.OrderStatus;
import com.barnizgallery.backend.enums.OrderType;

/**
 * An order in a list.
 */
public record OrderSummaryResponse(
        Long orderId,
        String orderCode,
        OrderType orderType,
        OrderStatus status,
        BigDecimal total,
        String currency,
        Instant paymentDueAt,
        Instant paidAt,
        Instant createdAt) {
}
