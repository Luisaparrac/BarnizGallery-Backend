package com.barnizgallery.backend.patterns.adapter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * What a gateway needs to charge one payment.
 *
 * @param reference     unique reference of the payment in this backend (sent to the gateway and back)
 * @param amount        amount to charge, with 2 decimals
 * @param currency      ISO 4217 code of the amount
 * @param customerEmail email of the buyer, or null
 * @param description   short text shown by the gateway
 * @param returnUrl     where the gateway sends the buyer after paying
 * @param cancelUrl     where the gateway sends the buyer after cancelling
 * @param expiresAt     moment after which the payment must not be accepted, or null
 */
public record CheckoutRequest(String reference, BigDecimal amount, String currency, String customerEmail,
        String description, String returnUrl, String cancelUrl, Instant expiresAt) {
}
