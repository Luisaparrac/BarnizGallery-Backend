package com.barnizgallery.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.PaymentDetailResponse;
import com.barnizgallery.backend.dto.PaymentResponse;
import com.barnizgallery.backend.dto.PaymentStartResponse;
import com.barnizgallery.backend.dto.SimulatedPaymentRequest;
import com.barnizgallery.backend.dto.StartPaymentRequest;
import com.barnizgallery.backend.patterns.adapter.WebhookCall;
import com.barnizgallery.backend.service.PaymentQueryService;
import com.barnizgallery.backend.service.PaymentSimulationService;
import com.barnizgallery.backend.service.PaymentStartService;
import com.barnizgallery.backend.service.PaymentWebhookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Payments of orders and the webhooks of the payment gateways.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Payments")
public class PaymentController {

    private final PaymentStartService startService;
    private final PaymentWebhookService webhookService;
    private final PaymentSimulationService simulationService;
    private final PaymentQueryService queryService;

    public PaymentController(PaymentStartService startService, PaymentWebhookService webhookService,
            PaymentSimulationService simulationService, PaymentQueryService queryService) {
        this.queryService = queryService;
        this.startService = startService;
        this.webhookService = webhookService;
        this.simulationService = simulationService;
    }

    @GetMapping("/payments/{paymentId}")
    @Operation(summary = "A payment of an own order (404 for someone else's)")
    public PaymentDetailResponse findPayment(@PathVariable Long paymentId) {
        return queryService.mine(paymentId);
    }

    @PostMapping("/payments/{paymentId}/simulate")
    @Operation(summary = "Demo: approve or decline an own payment of the simulated gateway (payments.test_mode)")
    public PaymentResponse simulate(@PathVariable Long paymentId,
            @Valid @RequestBody SimulatedPaymentRequest request) {
        return simulationService.simulate(paymentId, request.outcome());
    }

    /** Public: the gateway signs the call and its adapter verifies it (400 when the signature is wrong). */
    @PostMapping("/payments/webhooks/{gateway}")
    @Operation(summary = "Webhook of a payment gateway (wompi, paypal); idempotent by gateway event id")
    public void webhook(@PathVariable String gateway, @RequestHeader Map<String, String> headers,
            @RequestBody String body) {
        webhookService.receive(gateway, new WebhookCall(headers, body));
    }

    @PostMapping("/orders/{orderId}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Start paying an obligation of an own order; returns the gateway page to pay")
    public PaymentStartResponse start(@PathVariable Long orderId, @RequestBody StartPaymentRequest request) {
        return startService.start(orderId, request);
    }
}
