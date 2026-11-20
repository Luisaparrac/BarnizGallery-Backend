package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.barnizgallery.backend.enums.OrderStatus;
import com.barnizgallery.backend.enums.OrderType;
import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * An order with its lines and payment obligations. {@code shippingAddress} is the copy saved with the order.
 */
public record OrderResponse(
        Long orderId,
        String orderCode,
        OrderType orderType,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal shippingCost,
        BigDecimal total,
        String currency,
        @JsonRawValue String shippingAddress,
        Instant paymentDueAt,
        Instant paidAt,
        Instant createdAt,
        List<OrderItemResponse> items,
        List<PaymentObligationResponse> obligations) {
}
