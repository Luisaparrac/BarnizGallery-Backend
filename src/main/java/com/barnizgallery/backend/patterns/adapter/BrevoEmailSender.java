package com.barnizgallery.backend.patterns.adapter;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.barnizgallery.backend.config.BrevoProperties;

/**
 * <b>Adapter pattern – Adapter.</b>
 * <p>
 * Sends emails through Brevo's HTTP API ({@code POST /v3/smtp/email}, header {@code api-key}), without the
 * SDK. A failure of Brevo never breaks the user's flow: it is logged with its HTTP status only (no key and no
 * full address) and the email is lost.
 */
public class BrevoEmailSender implements EmailSender {

    static final String API_KEY_HEADER = "api-key";

    private static final Logger log = LoggerFactory.getLogger(BrevoEmailSender.class);

    private final RestClient restClient;
    private final BrevoProperties properties;

    /** @param restClient already configured with the Brevo base URL, the key header and the timeouts */
    public BrevoEmailSender(RestClient restClient, BrevoProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public void send(EmailMessage message) {
        BrevoEmailRequest body = new BrevoEmailRequest(
                new BrevoContact(properties.fromEmail(), properties.fromName()),
                List.of(new BrevoContact(message.toEmail(), message.toName())),
                message.subject(), message.html(), message.text());
        try {
            restClient.post().uri("/v3/smtp/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            log.warn("Brevo did not send the email to {}: HTTP {}", masked(message.toEmail()),
                    ex.getStatusCode().value());
        } catch (RestClientException ex) {
            log.warn("Brevo did not answer for the email to {}: {}", masked(message.toEmail()),
                    ex.getClass().getSimpleName());
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /** {@code ana@mail.com} → {@code a***@mail.com}, so logs do not keep full addresses. */
    static String masked(String email) {
        int at = email == null ? -1 : email.indexOf('@');
        return at < 1 ? "***" : email.charAt(0) + "***" + email.substring(at);
    }
}
