package com.barnizgallery.backend.patterns.decorator;

import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barnizgallery.backend.patterns.adapter.CheckoutRequest;
import com.barnizgallery.backend.patterns.adapter.CheckoutSession;
import com.barnizgallery.backend.patterns.adapter.GatewayEvent;
import com.barnizgallery.backend.patterns.adapter.PaymentGateway;
import com.barnizgallery.backend.patterns.adapter.WebhookCall;

/**
 * <b>Decorator pattern – Concrete decorator.</b>
 * <p>
 * Wraps any {@link PaymentGateway} (Wompi, PayPal or simulated) and adds an audit trail in the log: which payment
 * was sent to which gateway, how long it took, which webhook events arrived and which calls failed. The wrapped
 * gateway does the real work; the decorator has the same interface, so the registry hands it out unchanged.
 * Only references, amounts and statuses are logged: never keys, signatures or card data.
 */
public class LoggingPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(LoggingPaymentGateway.class);

    private final PaymentGateway wrapped;

    public LoggingPaymentGateway(PaymentGateway wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public String code() {
        return wrapped.code();
    }

    @Override
    public Set<String> currencies() {
        return wrapped.currencies();
    }

    @Override
    public boolean isEnabled() {
        return wrapped.isEnabled();
    }

    @Override
    public CheckoutSession startCheckout(CheckoutRequest request) {
        long start = System.nanoTime();
        try {
            CheckoutSession session = wrapped.startCheckout(request);
            log.info("Payment {} started in {} for {} {} ({} ms)", request.reference(), code(), request.amount(),
                    request.currency(), (System.nanoTime() - start) / 1_000_000);
            return session;
        } catch (RuntimeException ex) {
            log.warn("Payment {} could not start in {}: {}", request.reference(), code(), ex.getMessage());
            throw ex;
        }
    }

    @Override
    public Optional<GatewayEvent> readEvent(WebhookCall call) {
        try {
            Optional<GatewayEvent> event = wrapped.readEvent(call);
            event.ifPresentOrElse(
                    e -> log.info("Webhook {} event {} for {}: {}", code(), e.eventId(), e.reference(), e.status()),
                    () -> log.info("Webhook {} ignored: the event does not change a payment", code()));
            return event;
        } catch (RuntimeException ex) {
            log.warn("Webhook {} rejected: {}", code(), ex.getMessage());
            throw ex;
        }
    }

    /** The decorated gateway, for tests and diagnostics. */
    public PaymentGateway wrapped() {
        return wrapped;
    }
}
