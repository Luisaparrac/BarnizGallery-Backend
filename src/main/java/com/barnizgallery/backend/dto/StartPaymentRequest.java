package com.barnizgallery.backend.dto;

/**
 * Starts paying an obligation of an order. Without {@code obligationId} the first pending obligation is paid.
 * {@code simulated = true} uses the demo gateway (only while {@code payments.test_mode} is true).
 */
public record StartPaymentRequest(Long obligationId, Boolean simulated) {
}
