package com.barnizgallery.backend.patterns.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.barnizgallery.backend.dto.AiArtworkRef;
import com.barnizgallery.backend.dto.AiCandidate;
import com.barnizgallery.backend.dto.AiComparable;
import com.barnizgallery.backend.dto.AiDesignColor;
import com.barnizgallery.backend.dto.AiDesignRequest;
import com.barnizgallery.backend.dto.AiDesignResponse;
import com.barnizgallery.backend.dto.AiPriceSuggestion;
import com.barnizgallery.backend.dto.AiPricingPiece;
import com.barnizgallery.backend.dto.AiPricingRequest;
import com.barnizgallery.backend.dto.AiRecommendationRequest;
import com.barnizgallery.backend.dto.AiRecommendationResponse;
import com.barnizgallery.backend.dto.AiSimilarRequest;
import com.barnizgallery.backend.dto.AiSimilarResponse;
import com.barnizgallery.backend.exception.AiServiceException;

/** Uses the JSON examples of the AI service integration guide. No real call is made. */
class AiServiceClientTest {

    private static final String BASE_URL = "http://ai.test";

    private final String apiKey = UUID.randomUUID().toString();
    private MockRestServiceServer server;
    private AiServiceClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL)
                .defaultHeader(AiServiceClient.API_KEY_HEADER, apiKey);
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        client = new AiServiceClient(restClient, restClient);
    }

    @Test
    void suggestPriceSendsTheKeyAndSnakeCaseJson() {
        server.expect(requestTo(BASE_URL + "/v1/pricing/suggest"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(AiServiceClient.API_KEY_HEADER, apiKey))
                .andExpect(jsonPath("$.piece.shape").value("bandeja"))
                .andExpect(jsonPath("$.piece.width_cm").value(30))
                .andExpect(jsonPath("$.comparables[0].artwork_id").value(9))
                .andExpect(jsonPath("$.comparables[0].sold").value(true))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andRespond(withSuccess("""
                        {"suggested_price": 95, "min_price": 90, "max_price": 115, "currency": "USD",
                         "method": "comparables", "explanation": "Basado en 6 piezas parecidas"}""",
                        MediaType.APPLICATION_JSON));

        AiPriceSuggestion suggestion = client.suggestPrice(pricingRequest());

        assertThat(suggestion.suggestedPrice()).isEqualByComparingTo("95");
        assertThat(suggestion.minPrice()).isEqualByComparingTo("90");
        assertThat(suggestion.maxPrice()).isEqualByComparingTo("115");
        assertThat(suggestion.method()).isEqualTo("comparables");
        server.verify();
    }

    @Test
    void generateDesignsSendsTheRequestAndReadsTheImages() {
        server.expect(requestTo(BASE_URL + "/v1/designs"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(AiServiceClient.API_KEY_HEADER, apiKey))
                .andExpect(jsonPath("$.request_id").value(42))
                .andExpect(jsonPath("$.size_notes").value("unos 30 cm"))
                .andExpect(jsonPath("$.colors[0].hex").value("#B3261E"))
                .andExpect(jsonPath("$.count").value(1))
                .andRespond(withSuccess("""
                        {"provider": "fake", "model": "fake-1", "prompt": "Bandeja",
                         "images": [{"image_base64": "UklGRl", "mime_type": "image/webp", "composition": null,
                                     "prompt": "Bandeja roja"}]}""", MediaType.APPLICATION_JSON));

        AiDesignResponse answer = client.generateDesigns(new AiDesignRequest(42, "Bandeja para mi abuela", null,
                "Bandeja", "Tradicional", List.of(new AiDesignColor("Rojo", "#B3261E")), List.of("Colibrí"),
                "unos 30 cm", "es", 1));

        assertThat(answer.provider()).isEqualTo("fake");
        assertThat(answer.images()).singleElement().satisfies(image -> {
            assertThat(image.imageBase64()).isEqualTo("UklGRl");
            assertThat(image.mimeType()).isEqualTo("image/webp");
        });
        server.verify();
    }

    @Test
    void notEnoughDataKeepsStatusAndCode() {
        server.expect(requestTo(BASE_URL + "/v1/pricing/suggest"))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_CONTENT).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\": \"not_enough_data\", \"message\": \"Se necesitan al menos 3 piezas\"}"));

        assertThatThrownBy(() -> client.suggestPrice(pricingRequest()))
                .isInstanceOfSatisfying(AiServiceException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(422);
                    assertThat(ex.getCode()).isEqualTo("not_enough_data");
                    assertThat(ex.getMessage()).isEqualTo("Se necesitan al menos 3 piezas");
                });
    }

    @Test
    void unauthorizedIsTranslated() {
        server.expect(requestTo(BASE_URL + "/v1/recommendations"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\": \"unauthorized\", \"message\": \"Clave inválida\"}"));

        assertThatThrownBy(() -> client.recommend(recommendationRequest()))
                .isInstanceOfSatisfying(AiServiceException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(401);
                    assertThat(ex.getCode()).isEqualTo("unauthorized");
                });
    }

    @Test
    void errorWithoutJsonBodyGetsAGenericCode() {
        server.expect(requestTo(BASE_URL + "/v1/recommendations"))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY).contentType(MediaType.TEXT_HTML)
                        .body("<html>Bad gateway</html>"));

        assertThatThrownBy(() -> client.recommend(recommendationRequest()))
                .isInstanceOfSatisfying(AiServiceException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(502);
                    assertThat(ex.getCode()).isEqualTo("unknown_error");
                });
    }

    @Test
    void timeoutBecomesServiceUnreachable() {
        server.expect(requestTo(BASE_URL + "/v1/recommendations"))
                .andRespond(withException(new SocketTimeoutException("Read timed out")));

        assertThatThrownBy(() -> client.recommend(recommendationRequest()))
                .isInstanceOfSatisfying(AiServiceException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(503);
                    assertThat(ex.getCode()).isEqualTo("service_unreachable");
                    assertThat(ex.getMessage()).doesNotContain(apiKey);
                });
    }

    @Test
    void recommendReadsItemsAndNullBudget() {
        server.expect(requestTo(BASE_URL + "/v1/recommendations"))
                .andExpect(jsonPath("$.candidates[0].artwork_id").value(5))
                .andExpect(jsonPath("$.candidates[0].store_id").value(2))
                .andExpect(jsonPath("$.candidates[0].slug").value("bandeja-roja"))
                .andExpect(jsonPath("$.limit").value(10))
                .andRespond(withSuccess("""
                        {"strategy": "hybrid", "items": [
                          {"artwork_id": 5, "store_id": 2, "score": 0.9476,
                           "reasons": {"colors": ["rojo", "amarillo"], "shape": true, "style": true,
                                       "motifs": ["colibri"], "budget": null},
                           "explanation": "Coincide con lo que te gusta: rojo."}]}""",
                        MediaType.APPLICATION_JSON));

        AiRecommendationResponse response = client.recommend(recommendationRequest());

        assertThat(response.strategy()).isEqualTo("hybrid");
        assertThat(response.items()).singleElement().satisfies(item -> {
            assertThat(item.artworkId()).isEqualTo(5L);
            assertThat(item.storeId()).isEqualTo(2L);
            assertThat(item.score()).isEqualTo(0.9476);
            assertThat(item.reasons().colors()).containsExactly("rojo", "amarillo");
            assertThat(item.reasons().shape()).isTrue();
            assertThat(item.reasons().budget()).isNull();
            assertThat(item.explanation()).isEqualTo("Coincide con lo que te gusta: rojo.");
        });
    }

    @Test
    void similarSendsArtworkReferencesAndReadsTheSharedTraits() {
        server.expect(requestTo(BASE_URL + "/v1/similar"))
                .andExpect(jsonPath("$.artwork.artwork_id").value(12))
                .andExpect(jsonPath("$.artwork.slug").value("jarron-flora-azul"))
                .andExpect(jsonPath("$.candidates[0].slug").value("bargueno-colonial"))
                .andExpect(jsonPath("$.candidates[0].store_id").value(1))
                .andExpect(jsonPath("$.limit").value(6))
                .andRespond(withSuccess("""
                        {"artwork_id": 12, "slug": "jarron-flora-azul", "items": [
                          {"artwork_id": 13, "slug": "jarron-flora-dorada-fondo-negro", "store_id": 1,
                           "score": 0.8436, "shared_traits": ["flora", "forma de jarrón"], "same_master": true,
                           "explanation": "Comparte con «Jarrón Flora Azul»: flora y forma de jarrón."}]}""",
                        MediaType.APPLICATION_JSON));

        AiSimilarResponse response = client.similar(new AiSimilarRequest(new AiArtworkRef(12L, "jarron-flora-azul", 1L),
                List.of(new AiArtworkRef(11L, "bargueno-colonial", 1L)), 6));

        assertThat(response.slug()).isEqualTo("jarron-flora-azul");
        assertThat(response.items()).singleElement().satisfies(item -> {
            assertThat(item.artworkId()).isEqualTo(13L);
            assertThat(item.score()).isEqualTo(0.8436);
            assertThat(item.sharedTraits()).containsExactly("flora", "forma de jarrón");
            assertThat(item.sameMaster()).isTrue();
        });
    }

    private static AiPricingRequest pricingRequest() {
        AiPricingPiece piece = new AiPricingPiece("bandeja", "tradicional", List.of("rojo"), List.of("colibri"),
                new BigDecimal("30"), new BigDecimal("30"), new BigDecimal("3"), "barniz");
        AiComparable comparable = new AiComparable(9L, "bandeja", "tradicional", List.of("rojo"),
                List.of("colibri", "flores"), new BigDecimal("28"), new BigDecimal("28"), null, new BigDecimal("90"),
                "USD", true);
        return new AiPricingRequest(piece, List.of(comparable), "USD");
    }

    private static AiRecommendationRequest recommendationRequest() {
        AiCandidate candidate = new AiCandidate(5L, "bandeja-roja", 2L, List.of("rojo", "amarillo"), "bandeja", "tradicional",
                List.of("colibri"), new BigDecimal("80"), "USD", 12);
        return new AiRecommendationRequest(null, List.of(), List.of(candidate), 10);
    }
}
