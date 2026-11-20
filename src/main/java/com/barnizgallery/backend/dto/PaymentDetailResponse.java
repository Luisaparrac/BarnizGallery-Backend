package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.barnizgallery.backend.enums.PaymentStatus;

/**
 * A payment of an own order. {@code gateway} is upper case ({@code WOMPI}, {@code PAYPAL}, {@code SIMULATED});
 * {@code checkoutUrl} is not stored, so it is null here (it comes back when the payment is started).
 */
public record PaymentDetailResponse(
        Long id,
        Long orderId,
        Long obligationId,
        String gateway,
        String reference,
        String method,
        PaymentStatus status,
        BigDecimal amount,
        String currency,
        String checkoutUrl,
        Instant createdAt,
        Instant approvedAt) {
}
