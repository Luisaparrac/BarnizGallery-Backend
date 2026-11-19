package com.barnizgallery.backend.patterns.adapter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.barnizgallery.backend.dto.AiError;
import com.barnizgallery.backend.dto.AiPriceSuggestion;
import com.barnizgallery.backend.dto.AiPricingRequest;
import com.barnizgallery.backend.dto.AiRecommendationRequest;
import com.barnizgallery.backend.dto.AiRecommendationResponse;
import com.barnizgallery.backend.exception.AiServiceException;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * <b>Adapter pattern – Adapter.</b>
 * <p>
 * Translates {@link AiServiceGateway} calls into the HTTP API of the MUSEO AI service (JSON in snake_case).
 * The {@link RestClient} it receives already has the base URL, the {@value #API_KEY_HEADER} header and the
 * timeouts. Every failure becomes an {@link AiServiceException}:
 * <ul>
 * <li>an error answer keeps its HTTP status and the {@code code} of its body;</li>
 * <li>a timeout or network error becomes {@code 503 service_unreachable};</li>
 * <li>an empty or unreadable answer becomes {@code 502 invalid_response}.</li>
 * </ul>
 * Only the status and the code are logged, never the key.
 */
public class AiServiceClient implements AiServiceGateway {

    public static final String API_KEY_HEADER = "X-API-Key";

    private static final Logger log = LoggerFactory.getLogger(AiServiceClient.class);
    private static final JsonMapper ERROR_READER = JsonMapper.builder().build();

    private final RestClient restClient;

    public AiServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public AiPriceSuggestion suggestPrice(AiPricingRequest request) {
        return post("/v1/pricing/suggest", request, AiPriceSuggestion.class);
    }

    @Override
    public AiRecommendationResponse recommend(AiRecommendationRequest request) {
        return post("/v1/recommendations", request, AiRecommendationResponse.class);
    }

    private <T> T post(String path, Object body, Class<T> type) {
        T answer;
        try {
            answer = restClient.post().uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw toException(path, response);
                    })
                    .body(type);
        } catch (ResourceAccessException ex) {
            log.warn("AI service did not answer on {}: {}", path, ex.getMostSpecificCause().getClass().getSimpleName());
            throw new AiServiceException(503, "service_unreachable", "The AI service did not answer in time");
        } catch (RestClientException ex) {
            log.warn("AI service sent an unreadable answer on {}", path);
            throw new AiServiceException(502, "invalid_response", "The AI service sent an unreadable answer");
        }
        if (answer == null) {
            log.warn("AI service sent an empty answer on {}", path);
            throw new AiServiceException(502, "invalid_response", "The AI service sent an empty answer");
        }
        return answer;
    }

    private static AiServiceException toException(String path, ClientHttpResponse response) throws IOException {
        int status = response.getStatusCode().value();
        String code = "unknown_error";
        String message = "The AI service answered with HTTP " + status;
        try {
            AiError error = ERROR_READER.readValue(response.getBody(), AiError.class);
            if (error != null && error.code() != null) {
                code = error.code();
                message = error.message() == null ? message : error.message();
            }
        } catch (JacksonException ex) {
            // The body is not the documented error JSON: keep the generic code.
        }
        log.warn("AI service answered {} {} on {}", status, code, path);
        return new AiServiceException(status, code, message);
    }
}
