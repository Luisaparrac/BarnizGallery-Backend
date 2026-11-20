package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.barnizgallery.backend.enums.PayoutStatus;

/**
 * Money owed or paid to an artisan for delivered order lines, in one currency.
 */
public record PayoutResponse(
        Long payoutId,
        Long masterId,
        String masterName,
        String currency,
        BigDecimal amount,
        PayoutStatus status,
        String reference,
        Instant createdAt,
        Instant paidAt) {
}
