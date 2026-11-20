package com.barnizgallery.backend.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code app.payments.paypal.*} properties (PayPal REST API, Orders v2). PayPal charges the USD obligations.
 * Without the client id, the secret and the webhook id the gateway stays disabled.
 *
 * @param clientId     {@code PAYPAL_CLIENT_ID}
 * @param clientSecret {@code PAYPAL_CLIENT_SECRET}; only used to get OAuth tokens, never logged
 * @param webhookId    {@code PAYPAL_WEBHOOK_ID}, the id of the webhook created in the PayPal developer portal;
 *                     needed to verify webhook signatures
 * @param baseUrl      {@code PAYPAL_BASE_URL}, the sandbox by default
 * @param timeout      connect and read timeout of each call
 */
@ConfigurationProperties(prefix = "app.payments.paypal")
public record PayPalProperties(String clientId, String clientSecret, String webhookId, String baseUrl,
        Duration timeout) {

    /** True only when the client id, the secret and the webhook id are set. */
    public boolean isConfigured() {
        return notBlank(clientId) && notBlank(clientSecret) && notBlank(webhookId);
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
