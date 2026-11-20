package com.barnizgallery.backend.dto;

import java.math.BigDecimal;

import com.barnizgallery.backend.enums.PaymentStatus;

/**
 * A payment and its current status.
 */
public record PaymentResponse(Long paymentId, Long obligationId, String gateway, PaymentStatus status,
        BigDecimal amount, String currency) {
}
