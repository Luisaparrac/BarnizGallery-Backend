package com.barnizgallery.backend.dto;

import com.barnizgallery.backend.enums.PaymentStatus;

import jakarta.validation.constraints.NotNull;

/**
 * Result to simulate for a demo payment: {@code APPROVED} or {@code DECLINED}.
 */
public record SimulatedPaymentRequest(@NotNull PaymentStatus outcome) {
}
