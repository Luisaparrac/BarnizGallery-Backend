package com.barnizgallery.backend.patterns.adapter;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

import org.springframework.web.util.UriComponentsBuilder;

import com.barnizgallery.backend.config.WompiProperties;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.FeatureDisabledException;

/**
 * <b>Adapter pattern – Adapter</b> for Wompi (Colombia).
 * <p>
 * Wompi only charges COP. The buyer pays in the Wompi Web Checkout ({@code https://checkout.wompi.co/p/}),
 * opened with the public key, the amount in cents, the reference and the integrity signature computed here.
 */
public class WompiGateway implements PaymentGateway {

    public static final String CODE = "wompi";
    public static final String CURRENCY = "COP";

    private static final DateTimeFormatter EXPIRATION = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    private final WompiProperties properties;

    public WompiGateway(WompiProperties properties) {
        this.properties = properties;
    }

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public Set<String> currencies() {
        return Set.of(CURRENCY);
    }

    @Override
    public boolean isEnabled() {
        return properties.isConfigured();
    }

    @Override
    public CheckoutSession startCheckout(CheckoutRequest request) {
        if (!CURRENCY.equals(request.currency())) {
            throw new BusinessRuleException(ErrorCode.PAYMENT_CURRENCY_NOT_SUPPORTED, request.currency());
        }
        if (!isEnabled()) {
            throw new FeatureDisabledException(ErrorCode.PAYMENT_GATEWAY_DISABLED, CURRENCY);
        }
        long cents = WompiSignatures.amountInCents(request.amount());
        String expiration = request.expiresAt() == null ? null : EXPIRATION.format(request.expiresAt());
        String signature = WompiSignatures.integrity(request.reference(), cents, CURRENCY, expiration,
                properties.integritySecret());

        UriComponentsBuilder url = UriComponentsBuilder.fromUriString(properties.checkoutUrl())
                .queryParam("public-key", properties.publicKey())
                .queryParam("currency", CURRENCY)
                .queryParam("amount-in-cents", cents)
                .queryParam("reference", request.reference())
                .queryParam("signature:integrity", signature);
        if (request.returnUrl() != null) {
            url.queryParam("redirect-url", request.returnUrl());
        }
        if (expiration != null) {
            url.queryParam("expiration-time", expiration);
        }
        if (request.customerEmail() != null) {
            url.queryParam("customer-data:email", request.customerEmail());
        }
        return new CheckoutSession(CODE, request.reference(), url.encode().build().toUriString());
    }

    @Override
    public Optional<GatewayEvent> readEvent(WebhookCall call) {
        throw BusinessRuleException.badRequest(ErrorCode.WEBHOOK_NOT_SUPPORTED, CODE);
    }
}
