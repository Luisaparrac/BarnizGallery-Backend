package com.barnizgallery.backend.service;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.entity.Payment;
import com.barnizgallery.backend.entity.PaymentEvent;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.patterns.adapter.GatewayEvent;
import com.barnizgallery.backend.patterns.adapter.WebhookCall;
import com.barnizgallery.backend.repository.PaymentEventRepository;
import com.barnizgallery.backend.repository.PaymentRepository;

/**
 * Receives the webhooks of the gateways. The gateway adapter verifies the signature; then, in one transaction,
 * the event is saved in {@code payment_events} and applied to its payment. An event already received
 * (same gateway and event id) is ignored, so gateways can retry safely.
 */
@Service
public class PaymentWebhookService {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookService.class);

    private final PaymentGatewayRegistry gatewayRegistry;
    private final PaymentEventRepository eventRepository;
    private final PaymentRepository paymentRepository;
    private final Clock clock;

    public PaymentWebhookService(PaymentGatewayRegistry gatewayRegistry, PaymentEventRepository eventRepository,
            PaymentRepository paymentRepository, Clock clock) {
        this.gatewayRegistry = gatewayRegistry;
        this.eventRepository = eventRepository;
        this.paymentRepository = paymentRepository;
        this.clock = clock;
    }

    /** Verifies and applies a webhook call; events that do not change a payment are ignored. */
    @Transactional
    public void receive(String gatewayCode, WebhookCall call) {
        gatewayRegistry.byCode(gatewayCode).readEvent(call).ifPresent(this::process);
    }

    /** Saves and applies a verified event once; returns false when it was already received. */
    @Transactional
    public boolean process(GatewayEvent event) {
        if (eventRepository.existsByGatewayAndGatewayEventId(event.gateway(), event.eventId())) {
            log.info("Payment event {} of {} already received", event.eventId(), event.gateway());
            return false;
        }
        Payment payment = PaymentReferences.paymentId(event.reference())
                .flatMap(paymentRepository::findById)
                .filter(found -> found.getGateway().equals(event.gateway()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYMENT_NOT_FOUND, event.reference()));
        PaymentEvent saved = new PaymentEvent();
        saved.setPayment(payment);
        saved.setGateway(event.gateway());
        saved.setGatewayEventId(event.eventId());
        saved.setPayload(event.payload());
        saved.setReceivedAt(clock.instant());
        eventRepository.save(saved);
        return true;
    }
}
