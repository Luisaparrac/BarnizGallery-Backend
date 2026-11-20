package com.barnizgallery.backend.patterns.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.FeatureDisabledException;

import tools.jackson.databind.JsonNode;

/**
 * Authenticated JSON calls to the PayPal REST API: each call carries a fresh OAuth token and, when given, a
 * {@code PayPal-Request-Id} so PayPal answers a repeated call with the first result instead of doing it twice.
 * Any failure becomes 503 {@code payment_gateway_unavailable}; only the HTTP status is logged.
 */
public class PayPalApiClient {

    static final String REQUEST_ID_HEADER = "PayPal-Request-Id";

    private static final Logger log = LoggerFactory.getLogger(PayPalApiClient.class);

    private final RestClient restClient;
    private final PayPalTokenClient tokenClient;

    public PayPalApiClient(RestClient restClient, PayPalTokenClient tokenClient) {
        this.restClient = restClient;
        this.tokenClient = tokenClient;
    }

    /** @param requestId idempotency key, or null for calls that do not need one */
    public JsonNode post(String path, String requestId, Object body) {
        try {
            JsonNode answer = restClient.post().uri(path)
                    .headers(headers -> {
                        headers.setBearerAuth(tokenClient.accessToken());
                        if (requestId != null) {
                            headers.set(REQUEST_ID_HEADER, requestId);
                        }
                    })
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            if (answer == null) {
                throw new FeatureDisabledException(ErrorCode.PAYMENT_GATEWAY_UNAVAILABLE);
            }
            return answer;
        } catch (RestClientResponseException ex) {
            log.warn("PayPal answered HTTP {} on {}", ex.getStatusCode().value(), path);
            throw new FeatureDisabledException(ErrorCode.PAYMENT_GATEWAY_UNAVAILABLE);
        } catch (RestClientException ex) {
            log.warn("PayPal did not answer on {}: {}", path, ex.getClass().getSimpleName());
            throw new FeatureDisabledException(ErrorCode.PAYMENT_GATEWAY_UNAVAILABLE);
        }
    }
}
