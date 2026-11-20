package com.barnizgallery.backend.patterns.adapter;

import com.barnizgallery.backend.enums.PaymentStatus;

/**
 * A verified webhook event that changes a payment.
 *
 * @param gateway              code of the gateway
 * @param eventId              id of the event in the gateway; with the gateway it makes the event idempotent
 *                             ({@code payment_events.gateway_event_id})
 * @param reference            reference of the payment in this backend
 * @param gatewayTransactionId id of the transaction or capture in the gateway
 * @param status               new status of the payment
 * @param payload              raw body, kept in {@code payment_events.payload}
 */
public record GatewayEvent(String gateway, String eventId, String reference, String gatewayTransactionId,
        PaymentStatus status, String payload) {
}
