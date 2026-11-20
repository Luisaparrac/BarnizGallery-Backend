package com.barnizgallery.backend.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code app.mail.brevo.*} properties. Without the API key and the sender address, emails are not sent (the
 * account flows keep working, but the user receives no email).
 *
 * @param apiKey    {@code BREVO_API_KEY}, sent in the {@code api-key} header
 * @param fromEmail {@code MAIL_FROM}, a sender verified in Brevo
 * @param fromName  {@code MAIL_FROM_NAME}
 * @param baseUrl   Brevo API base URL
 * @param timeout   connect and read timeout of each call
 */
@ConfigurationProperties(prefix = "app.mail.brevo")
public record BrevoProperties(String apiKey, String fromEmail, String fromName, String baseUrl,
        Duration timeout) {

    /** True only when the API key and the sender address are set. */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank() && fromEmail != null && !fromEmail.isBlank();
    }
}
