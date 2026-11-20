package com.barnizgallery.backend.patterns.adapter;

import java.util.List;

import com.barnizgallery.backend.config.PayPalProperties;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Checks that a webhook really comes from PayPal, with PayPal's own {@code POST
 * /v1/notifications/verify-webhook-signature}: it receives the five {@code PAYPAL-*} transmission headers, the id
 * of our webhook and the event, and answers {@code verification_status} {@code SUCCESS} or {@code FAILURE}.
 */
public class PayPalWebhookVerifier {

    static final List<String> HEADERS = List.of("PAYPAL-AUTH-ALGO", "PAYPAL-CERT-URL", "PAYPAL-TRANSMISSION-ID",
            "PAYPAL-TRANSMISSION-SIG", "PAYPAL-TRANSMISSION-TIME");
    static final List<String> FIELDS = List.of("auth_algo", "cert_url", "transmission_id", "transmission_sig",
            "transmission_time");

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final PayPalApiClient api;
    private final PayPalProperties properties;

    public PayPalWebhookVerifier(PayPalApiClient api, PayPalProperties properties) {
        this.api = api;
        this.properties = properties;
    }

    /** False when a header is missing or PayPal does not confirm the signature. */
    public boolean isValid(WebhookCall call, JsonNode event) {
        ObjectNode body = JSON.createObjectNode();
        for (int i = 0; i < HEADERS.size(); i++) {
            String value = call.header(HEADERS.get(i));
            if (value == null || value.isBlank()) {
                return false;
            }
            body.put(FIELDS.get(i), value);
        }
        body.put("webhook_id", properties.webhookId());
        body.set("webhook_event", event);
        JsonNode answer = api.post("/v1/notifications/verify-webhook-signature", null, body);
        return "SUCCESS".equals(answer.path("verification_status").asString());
    }
}
