package com.barnizgallery.backend.service;

import java.util.EnumSet;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.PaymentResponse;
import com.barnizgallery.backend.entity.Payment;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.PaymentStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.patterns.adapter.GatewayEvent;
import com.barnizgallery.backend.patterns.adapter.SimulatedGateway;
import com.barnizgallery.backend.repository.PaymentRepository;
import com.barnizgallery.backend.security.CurrentUser;

/**
 * Confirms or declines a demo payment of the simulated gateway, which has no webhooks. The result goes through the
 * same path as a real webhook event (saved in {@code payment_events} and applied), so the database reacts the same
 * way. Only the buyer can do it, and only while {@code payments.test_mode} is true.
 */
@Service
public class PaymentSimulationService {

    private final PaymentRepository paymentRepository;
    private final PaymentWebhookService webhookService;
    private final PaymentSettings paymentSettings;
    private final CurrentUser currentUser;

    public PaymentSimulationService(PaymentRepository paymentRepository, PaymentWebhookService webhookService,
            PaymentSettings paymentSettings, CurrentUser currentUser) {
        this.paymentRepository = paymentRepository;
        this.webhookService = webhookService;
        this.paymentSettings = paymentSettings;
        this.currentUser = currentUser;
    }

    @Transactional
    public PaymentResponse simulate(Long paymentId, PaymentStatus outcome) {
        Long buyerId = currentUser.requireId();
        Payment payment = paymentRepository.findById(paymentId)
                .filter(p -> p.getObligation().getOrder().getBuyer().getUserId().equals(buyerId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYMENT_NOT_FOUND, paymentId));
        if (!SimulatedGateway.CODE.equals(payment.getGateway())) {
            throw new BusinessRuleException(ErrorCode.PAYMENT_NOT_SIMULATED, paymentId);
        }
        if (!paymentSettings.testMode()) {
            throw new BusinessRuleException(ErrorCode.SIMULATED_PAYMENTS_DISABLED);
        }
        if (!EnumSet.of(PaymentStatus.APPROVED, PaymentStatus.DECLINED).contains(outcome)) {
            throw BusinessRuleException.unprocessable(ErrorCode.SIMULATED_OUTCOME_INVALID);
        }
        String eventId = "sim-" + UUID.randomUUID();
        webhookService.process(new GatewayEvent(SimulatedGateway.CODE, eventId,
                PaymentReferences.of(paymentId), "simtx-" + paymentId, outcome,
                "{\"simulated\":true,\"outcome\":\"" + outcome.name() + "\",\"event\":\"" + eventId + "\"}"));
        return new PaymentResponse(payment.getPaymentId(), payment.getObligation().getObligationId(),
                payment.getGateway(), payment.getStatus(), payment.getAmount(), payment.getCurrency());
    }
}
