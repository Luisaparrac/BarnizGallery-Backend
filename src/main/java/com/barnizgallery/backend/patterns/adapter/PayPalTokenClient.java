package com.barnizgallery.backend.patterns.adapter;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.barnizgallery.backend.config.PayPalProperties;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.FeatureDisabledException;

/**
 * Gets OAuth 2.0 access tokens for the PayPal REST API ({@code POST /v1/oauth2/token} with
 * {@code grant_type=client_credentials} and HTTP Basic auth of the client id and secret). A token is reused
 * until one minute before it expires.
 */
public class PayPalTokenClient {

    static final Duration SAFETY_MARGIN = Duration.ofMinutes(1);

    private static final Logger log = LoggerFactory.getLogger(PayPalTokenClient.class);

    private final RestClient restClient;
    private final PayPalProperties properties;
    private final Clock clock;
    private String token;
    private Instant validUntil = Instant.MIN;

    public PayPalTokenClient(RestClient restClient, PayPalProperties properties, Clock clock) {
        this.restClient = restClient;
        this.properties = properties;
        this.clock = clock;
    }

    /** A valid access token, requesting a new one only when the cached one is about to expire. */
    public synchronized String accessToken() {
        Instant now = clock.instant();
        if (token != null && now.isBefore(validUntil)) {
            return token;
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        PayPalTokenResponse answer;
        try {
            answer = restClient.post().uri("/v1/oauth2/token")
                    .headers(headers -> headers.setBasicAuth(properties.clientId(), properties.clientSecret()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(form)
                    .retrieve()
                    .body(PayPalTokenResponse.class);
        } catch (RestClientResponseException ex) {
            log.warn("PayPal refused the OAuth token request: HTTP {}", ex.getStatusCode().value());
            throw new FeatureDisabledException(ErrorCode.PAYMENT_GATEWAY_UNAVAILABLE);
        } catch (RestClientException ex) {
            log.warn("PayPal did not answer the OAuth token request: {}", ex.getClass().getSimpleName());
            throw new FeatureDisabledException(ErrorCode.PAYMENT_GATEWAY_UNAVAILABLE);
        }
        if (answer == null || answer.accessToken() == null) {
            throw new FeatureDisabledException(ErrorCode.PAYMENT_GATEWAY_UNAVAILABLE);
        }
        token = answer.accessToken();
        validUntil = now.plusSeconds(answer.expiresIn()).minus(SAFETY_MARGIN);
        return token;
    }
}
