package com.barnizgallery.backend.patterns.adapter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;

/**
 * <b>Adapter pattern – Adapter</b> for demos.
 * <p>
 * Charges nothing and talks to no provider: it accepts any currency and sends the buyer to a simulated payment
 * page of the frontend. It has no webhooks; demo payments are confirmed by the backend itself.
 */
@Component
public class SimulatedGateway implements PaymentGateway {

    public static final String CODE = "simulated";

    private final String frontendUrl;

    public SimulatedGateway(@Value("${app.frontend-url}") String frontendUrl) {
        this.frontendUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1)
                : frontendUrl;
    }

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public Set<String> currencies() {
        return Set.of();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public CheckoutSession startCheckout(CheckoutRequest request) {
        String reference = URLEncoder.encode(request.reference(), StandardCharsets.UTF_8);
        return new CheckoutSession(CODE, "sim_" + request.reference(),
                frontendUrl + "/payments/simulated?reference=" + reference);
    }

    @Override
    public Optional<GatewayEvent> readEvent(WebhookCall call) {
        throw BusinessRuleException.badRequest(ErrorCode.WEBHOOK_NOT_SUPPORTED, CODE);
    }
}
