package com.barnizgallery.backend.patterns.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.SocketTimeoutException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.barnizgallery.backend.config.CloudinaryProperties;
import com.barnizgallery.backend.exception.FeatureDisabledException;

/** Cloudinary is simulated with MockRestServiceServer; no real call is made. */
class CloudinaryStorageServiceTest {

    private static final String BASE_URL = "https://cloudinary.test";
    private static final Instant NOW = Instant.parse("2026-11-19T12:00:00Z");

    private final String apiKey = UUID.randomUUID().toString();
    private final String apiSecret = UUID.randomUUID().toString();
    private final CloudinaryProperties properties = new CloudinaryProperties("demo-cloud", apiKey, apiSecret,
            BASE_URL, "museo", Duration.ofSeconds(30));
    private MockRestServiceServer server;
    private CloudinaryStorageService storage;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        storage = new CloudinaryStorageService(builder.build(), properties, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void uploadSendsASignedRequestAndReadsTheImage() {
        String signature = new CloudinarySigner(apiSecret)
                .sign(Map.of("folder", "museo/artworks/3", "timestamp", String.valueOf(NOW.getEpochSecond())));
        server.expect(requestTo(BASE_URL + "/v1_1/demo-cloud/image/upload"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(allOf(containsString("museo/artworks/3"), containsString(apiKey),
                        containsString(signature), not(containsString(apiSecret)))))
                .andRespond(withSuccess("""
                        {"public_id": "museo/artworks/3/abc", "secure_url": "https://res.test/abc.jpg",
                         "width": 800, "height": 600, "format": "jpg"}""", MediaType.APPLICATION_JSON));

        StoredImage image = storage.upload(new MockMultipartFile("file", "front.jpg", "image/jpeg",
                new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF }), "artworks/3");

        assertThat(image).isEqualTo(new StoredImage("museo/artworks/3/abc", "https://res.test/abc.jpg", 800, 600));
        server.verify();
    }

    @Test
    void cloudinaryErrorsBecomeStorageUnavailable() {
        server.expect(requestTo(BASE_URL + "/v1_1/demo-cloud/image/upload"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\": {\"message\": \"Invalid Signature\"}}"));

        assertThatThrownBy(() -> storage.upload(new MockMultipartFile("file", "a.jpg", "image/jpeg",
                new byte[] { 1 }), "artworks/3"))
                .isInstanceOf(FeatureDisabledException.class)
                .hasMessage("storage_unavailable");
    }

    @Test
    void timeoutsBecomeStorageUnavailable() {
        server.expect(requestTo(BASE_URL + "/v1_1/demo-cloud/image/destroy"))
                .andRespond(withException(new SocketTimeoutException("Read timed out")));

        assertThatThrownBy(() -> storage.delete("museo/artworks/3/abc")).hasMessage("storage_unavailable");
    }

    @Test
    void deleteSendsTheSignedPublicIdAndAcceptsImagesAlreadyGone() {
        String signature = new CloudinarySigner(apiSecret)
                .sign(Map.of("public_id", "museo/artworks/3/abc", "timestamp", String.valueOf(NOW.getEpochSecond())));
        server.expect(requestTo(BASE_URL + "/v1_1/demo-cloud/image/destroy"))
                .andExpect(content().formDataContains(Map.of("public_id", "museo/artworks/3/abc",
                        "api_key", apiKey, "signature", signature)))
                .andRespond(withSuccess("{\"result\": \"ok\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE_URL + "/v1_1/demo-cloud/image/destroy"))
                .andRespond(withSuccess("{\"result\": \"not found\"}", MediaType.APPLICATION_JSON));

        assertThatCode(() -> storage.delete("museo/artworks/3/abc")).doesNotThrowAnyException();
        assertThatCode(() -> storage.delete("museo/artworks/3/abc")).doesNotThrowAnyException();
        server.verify();
    }
}
