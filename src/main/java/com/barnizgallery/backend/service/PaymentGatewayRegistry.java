package com.barnizgallery.backend.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.FeatureDisabledException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.patterns.adapter.PaymentGateway;
import com.barnizgallery.backend.patterns.adapter.SimulatedGateway;

/**
 * Chooses the payment gateway. A real payment uses the gateway of the obligation currency (COP → Wompi,
 * USD → PayPal); the simulated gateway is asked for by code, for demos. Webhooks find their gateway by code.
 */
@Component
public class PaymentGatewayRegistry {

    private final List<PaymentGateway> gateways;

    public PaymentGatewayRegistry(List<PaymentGateway> gateways) {
        this.gateways = List.copyOf(gateways);
    }

    /**
     * The real gateway that charges this currency.
     *
     * @throws BusinessRuleException   409 {@code payment_currency_not_supported} when no real gateway takes it
     * @throws FeatureDisabledException 503 {@code payment_gateway_disabled} when its gateway has no credentials
     */
    public PaymentGateway forCurrency(String currency) {
        String code = currency == null ? "" : currency.trim().toUpperCase(Locale.ROOT);
        PaymentGateway gateway = gateways.stream()
                .filter(g -> g.currencies().contains(code))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException(ErrorCode.PAYMENT_CURRENCY_NOT_SUPPORTED, code));
        if (!gateway.isEnabled()) {
            throw new FeatureDisabledException(ErrorCode.PAYMENT_GATEWAY_DISABLED, code);
        }
        return gateway;
    }

    /** The gateway with this code ({@code wompi}, {@code paypal} or {@code simulated}), for webhooks and demos. */
    public PaymentGateway byCode(String code) {
        return gateways.stream()
                .filter(g -> g.code().equals(code))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PAYMENT_GATEWAY_NOT_FOUND, code));
    }

    public PaymentGateway simulated() {
        return byCode(SimulatedGateway.CODE);
    }
}
