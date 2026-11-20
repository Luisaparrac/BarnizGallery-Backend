package com.barnizgallery.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.PaymentStartResponse;
import com.barnizgallery.backend.dto.StartPaymentRequest;
import com.barnizgallery.backend.patterns.adapter.WebhookCall;
import com.barnizgallery.backend.service.PaymentStartService;
import com.barnizgallery.backend.service.PaymentWebhookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Payments of orders and the webhooks of the payment gateways.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Payments")
public class PaymentController {

    private final PaymentStartService startService;
    private final PaymentWebhookService webhookService;

    public PaymentController(PaymentStartService startService, PaymentWebhookService webhookService) {
        this.startService = startService;
        this.webhookService = webhookService;
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
