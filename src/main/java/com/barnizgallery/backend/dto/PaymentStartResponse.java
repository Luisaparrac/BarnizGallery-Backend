package com.barnizgallery.backend.dto;

import java.math.BigDecimal;

import com.barnizgallery.backend.enums.PaymentStatus;

/**
 * A payment just started: the buyer must go to {@code redirectUrl} to pay. {@code reference} identifies the
 * payment in the gateway and in the webhooks.
 */
public record PaymentStartResponse(
        Long paymentId,
        Long obligationId,
        String gateway,
        String reference,
        String redirectUrl,
        BigDecimal amount,
        String currency,
        PaymentStatus status) {
}
