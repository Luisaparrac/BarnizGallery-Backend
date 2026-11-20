package com.barnizgallery.backend.patterns.adapter;

import java.util.Optional;
import java.util.Set;

/**
 * <b>Adapter pattern – Target.</b>
 * <p>
 * The interface the system uses to charge a payment obligation. Each payment provider has its own API; an
 * adapter per provider translates these calls: Wompi (COP), PayPal (USD) and a simulated gateway for demos.
 * The gateway of a payment is chosen by the currency of its obligation.
 */
public interface PaymentGateway {

    /** Code saved in {@code payments.gateway}, for example {@code wompi}. */
    String code();

    /** Currencies this gateway can charge; empty means any currency (only the simulated gateway). */
    Set<String> currencies();

    /** False when its credentials are not configured. */
    boolean isEnabled();

    /** Prepares the payment and returns where the buyer must go to pay. */
    CheckoutSession startCheckout(CheckoutRequest request);

    /**
     * Verifies the signature of a webhook call and reads it. Fails with 400 {@code invalid_webhook_signature}
     * when the signature is wrong; returns empty for valid events that do not change a payment.
     */
    Optional<GatewayEvent> readEvent(WebhookCall call);
}
