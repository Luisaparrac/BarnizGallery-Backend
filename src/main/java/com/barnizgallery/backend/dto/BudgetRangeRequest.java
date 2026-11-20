package com.barnizgallery.backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Creates or edits a budget range. {@code code} is only read when creating; a null {@code maxAmount} means
 * "more than {@code minAmount}".
 */
public record BudgetRangeRequest(
        @Pattern(regexp = "[a-z0-9][a-z0-9_+-]{0,59}") String code,
        @NotNull @PositiveOrZero BigDecimal minAmount,
        @PositiveOrZero BigDecimal maxAmount,
        @NotNull @Pattern(regexp = "[A-Z]{3}") String currency) {
}
