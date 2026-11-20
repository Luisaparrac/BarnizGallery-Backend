package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.barnizgallery.backend.enums.PaymentObligationStage;
import com.barnizgallery.backend.enums.PaymentObligationStatus;

/**
 * Something the buyer has to pay for an order: the single payment, or the deposit and balance of a custom order.
 */
public record PaymentObligationResponse(
        Long obligationId,
        PaymentObligationStage stage,
        BigDecimal amount,
        String currency,
        PaymentObligationStatus status,
        Instant dueAt) {
}
