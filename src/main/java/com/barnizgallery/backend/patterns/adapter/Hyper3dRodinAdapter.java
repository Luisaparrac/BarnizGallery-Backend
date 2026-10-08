package com.barnizgallery.backend.patterns.adapter;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.barnizgallery.backend.config.Hyper3dProperties;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.FeatureDisabledException;
import com.barnizgallery.backend.model.enums.GenerationStatus;

import tools.jackson.databind.JsonNode;

/**
 * <b>Adapter pattern – Adapter.</b>
 * <p>
 * Implements our {@link ThreeDModelGenerator} interface using the Hyper3D Rodin REST API
 * (https://docs.hyper3d.ai):
 * <ul>
 * <li>{@code POST /api/v2/rodin} – multipart with {@code images} and {@code tier}; returns
 * {@code uuid} and {@code jobs.subscription_key}</li>
 * <li>{@code POST /api/v2/status} – {@code {subscription_key}}; returns {@code jobs[].status}
 * (Waiting, Generating, Done, Failed)</li>
 * <li>{@code POST /api/v2/download} – {@code {task_uuid}}; returns {@code list[]} of {@code {url, name}}</li>
 * </ul>
 * Authentication: {@code Authorization: Bearer <HYPER3D_API_KEY>}. Without a key the adapter is
 * disabled and the endpoints answer HTTP 503.
 */
@Component
public class Hyper3dRodinAdapter implements ThreeDModelGenerator {

    static final String TIER = "Gen-2.5-Medium";
    static final int MAX_IMAGES = 5;

    private final String apiKey;
    private final RestClient api;
    private final RestClient downloader;

    public Hyper3dRodinAdapter(Hyper3dProperties properties) {
        this.apiKey = properties.apiKey();
        this.api = RestClient.builder()
                .baseUrl(properties.baseUrl() + "/api/v2")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
        this.downloader = RestClient.create();
    }

    @Override
    public boolean isEnabled() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public GenerationTicket submit(List<String> photoUrls) {
        requireEnabled();
        MultipartBodyBuilder form = new MultipartBodyBuilder();
        int index = 0;
        for (String url : photoUrls.stream().limit(MAX_IMAGES).toList()) {
            form.part("images", downloadImage(url)).filename("photo-" + index++ + extensionOf(url));
        }
        form.part("tier", TIER);
        form.part("geometry_file_format", "glb");

        JsonNode response = call(() -> api.post().uri("/rodin")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(form.build())
                .retrieve()
                .body(JsonNode.class));
        String uuid = text(response, "uuid");
        String error = text(response, "error");
        if (uuid == null || uuid.isBlank() || (error != null && !error.isBlank())) {
            throw new BusinessRuleException("Hyper3D rejected the task: "
                    + (response == null ? "empty response" : error), HttpStatus.BAD_GATEWAY);
        }
        String subscriptionKey = text(response.path("jobs"), "subscription_key");
        return new GenerationTicket(uuid, subscriptionKey);
    }

    @Override
    public GenerationStatus checkStatus(GenerationTicket ticket) {
        requireEnabled();
        if (ticket.subscriptionKey() == null) {
            // Only the task uuid could be stored: the task is done when its GLB can be downloaded.
            return fetchGlbUrl(ticket).isPresent() ? GenerationStatus.COMPLETED : GenerationStatus.PROCESSING;
        }
        JsonNode response = call(() -> api.post().uri("/status")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("subscription_key", ticket.subscriptionKey()))
                .retrieve()
                .body(JsonNode.class));
        List<String> statuses = new ArrayList<>();
        if (response != null) {
            for (JsonNode job : response.path("jobs")) {
                statuses.add(text(job, "status"));
            }
        }
        return mapStatuses(statuses);
    }

    @Override
    public Optional<String> fetchGlbUrl(GenerationTicket ticket) {
        requireEnabled();
        JsonNode response = call(() -> api.post().uri("/download")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("task_uuid", ticket.taskUuid()))
                .retrieve()
                .body(JsonNode.class));
        if (response == null) {
            return Optional.empty();
        }
        for (JsonNode item : response.path("list")) {
            String name = text(item, "name");
            if (name != null && name.toLowerCase(Locale.ROOT).endsWith(".glb")) {
                return Optional.ofNullable(text(item, "url"));
            }
        }
        return Optional.empty();
    }

    /** Translates the Hyper3D job statuses to our enum: all Done → COMPLETED, any Failed → FAILED. */
    static GenerationStatus mapStatuses(List<String> statuses) {
        if (statuses.isEmpty()) {
            return GenerationStatus.PROCESSING;
        }
        if (statuses.stream().anyMatch("Failed"::equalsIgnoreCase)) {
            return GenerationStatus.FAILED;
        }
        if (statuses.stream().allMatch("Done"::equalsIgnoreCase)) {
            return GenerationStatus.COMPLETED;
        }
        return GenerationStatus.PROCESSING;
    }

    /** Text value of a JSON field, or null when it is missing or not a string. */
    private static String text(JsonNode node, String field) {
        return node == null ? null : node.path(field).stringValue(null);
    }

    /** Hyper3D receives image files, so each photo URL is downloaded first. */
    private byte[] downloadImage(String url) {
        if (url == null || !(url.startsWith("http://") || url.startsWith("https://"))) {
            throw BusinessRuleException.unprocessable(
                    "Photo URL must be absolute (http/https) to send it to Hyper3D: " + url);
        }
        byte[] bytes = call(() -> downloader.get().uri(URI.create(url)).retrieve().body(byte[].class));
        if (bytes == null || bytes.length == 0) {
            throw BusinessRuleException.unprocessable("Photo could not be downloaded: " + url);
        }
        return bytes;
    }

    private static String extensionOf(String url) {
        String path = URI.create(url).getPath().toLowerCase(Locale.ROOT);
        int dot = path.lastIndexOf('.');
        return dot >= 0 && path.length() - dot <= 5 ? path.substring(dot) : ".jpg";
    }

    private void requireEnabled() {
        if (!isEnabled()) {
            throw new FeatureDisabledException("Hyper3D is disabled: HYPER3D_API_KEY is not configured");
        }
    }

    private static <T> T call(Supplier<T> request) {
        try {
            return request.get();
        } catch (RestClientException ex) {
            throw new BusinessRuleException("Error calling an external service: " + ex.getMessage(),
                    HttpStatus.BAD_GATEWAY);
        }
    }
}
