package com.barnizgallery.backend.service;

import java.util.Optional;

/**
 * The reference this backend sends to the gateways for a payment ({@code PAY-<paymentId>}) and its way back.
 */
final class PaymentReferences {

    private static final String PREFIX = "PAY-";

    private PaymentReferences() {
    }

    static String of(Long paymentId) {
        return PREFIX + paymentId;
    }

    static Optional<Long> paymentId(String reference) {
        if (reference == null || !reference.startsWith(PREFIX)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(reference.substring(PREFIX.length())));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}
