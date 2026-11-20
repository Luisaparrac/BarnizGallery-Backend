package com.barnizgallery.backend.dto;

import java.math.BigDecimal;

/**
 * A budget range of the taste questionnaire; {@code maxAmount} is null for "more than {@code minAmount}".
 */
public record BudgetRangeResponse(
        String code,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        String currency,
        Long id,
        boolean isActive) {
}
