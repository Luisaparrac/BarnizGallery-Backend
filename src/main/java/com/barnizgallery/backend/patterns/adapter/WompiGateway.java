package com.barnizgallery.backend.patterns.adapter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.springframework.web.util.UriComponentsBuilder;

import com.barnizgallery.backend.config.WompiProperties;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.PaymentStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.FeatureDisabledException;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * <b>Adapter pattern – Adapter</b> for Wompi (Colombia).
 * <p>
 * Wompi only charges COP. The buyer pays in the Wompi Web Checkout ({@code https://checkout.wompi.co/p/}),
 * opened with the public key, the amount in cents, the reference and the integrity signature computed here.
 * Wompi then reports the result with signed events ({@code transaction.updated}).
 */
public class WompiGateway implements PaymentGateway {

    public static final String CODE = "wompi";
    public static final String CURRENCY = "COP";

    static final String CHECKSUM_HEADER = "X-Event-Checksum";
    static final String TRANSACTION_UPDATED = "transaction.updated";

    private static final JsonMapper JSON = JsonMapper.builder().build();
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

    /**
     * Verifies a Wompi event: the values of {@code signature.properties} (paths inside {@code data}), then the
     * {@code timestamp} and the events secret, hashed with SHA-256, must equal the checksum of the
     * {@code X-Event-Checksum} header (or {@code signature.checksum}). Only {@code transaction.updated} changes a
     * payment; its event id is the transaction id with its status, so a retried event is not applied twice.
     */
    @Override
    public Optional<GatewayEvent> readEvent(WebhookCall call) {
        if (!isEnabled()) {
            throw new FeatureDisabledException(ErrorCode.PAYMENT_GATEWAY_DISABLED, CURRENCY);
        }
        JsonNode event = parse(call.body());
        String received = call.header(CHECKSUM_HEADER) != null ? call.header(CHECKSUM_HEADER)
                : event.path("signature").path("checksum").asString();
        StringBuilder signed = new StringBuilder();
        for (JsonNode property : event.path("signature").path("properties").values()) {
            signed.append(valueAt(event.path("data"), property.asString()));
        }
        signed.append(event.path("timestamp").asString()).append(properties.eventsSecret());
        String expected = WompiSignatures.sha256Hex(signed.toString());
        if (received == null || received.isBlank() || !MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.US_ASCII),
                received.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII))) {
            throw BusinessRuleException.badRequest(ErrorCode.INVALID_WEBHOOK_SIGNATURE);
        }
        if (!TRANSACTION_UPDATED.equals(event.path("event").asString())) {
            return Optional.empty();
        }
        JsonNode transaction = event.path("data").path("transaction");
        String transactionId = transaction.path("id").asString();
        String status = transaction.path("status").asString();
        return Optional.of(new GatewayEvent(CODE, transactionId + ":" + status,
                transaction.path("reference").asString(), transactionId, toPaymentStatus(status), call.body()));
    }

    /** APPROVED → approved; DECLINED, VOIDED and ERROR → declined; anything else → pending. */
    static PaymentStatus toPaymentStatus(String wompiStatus) {
        return switch (wompiStatus) {
            case "APPROVED" -> PaymentStatus.APPROVED;
            case "DECLINED", "VOIDED", "ERROR" -> PaymentStatus.DECLINED;
            default -> PaymentStatus.PENDING;
        };
    }

    private static String valueAt(JsonNode data, String path) {
        JsonNode node = data;
        for (String part : path.split("\\.")) {
            node = node.path(part);
        }
        return node.isMissingNode() || node.isNull() ? "" : node.asString();
    }

    private static JsonNode parse(String body) {
        try {
            return JSON.readTree(body);
        } catch (JacksonException ex) {
            throw BusinessRuleException.badRequest(ErrorCode.INVALID_WEBHOOK_SIGNATURE);
        }
    }
}
