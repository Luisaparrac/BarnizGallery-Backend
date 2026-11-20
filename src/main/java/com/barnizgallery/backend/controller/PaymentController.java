package com.barnizgallery.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.PaymentStartResponse;
import com.barnizgallery.backend.dto.StartPaymentRequest;
import com.barnizgallery.backend.service.PaymentStartService;

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

    public PaymentController(PaymentStartService startService) {
        this.startService = startService;
    }

    @PostMapping("/orders/{orderId}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Start paying an obligation of an own order; returns the gateway page to pay")
    public PaymentStartResponse start(@PathVariable Long orderId, @RequestBody StartPaymentRequest request) {
        return startService.start(orderId, request);
    }
}
