package com.barnizgallery.backend.patterns.adapter;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.barnizgallery.backend.config.AppProperties;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.FeatureDisabledException;
import com.barnizgallery.backend.model.enums.GenerationStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    public Hyper3dRodinAdapter(AppProperties properties) {
        this.apiKey = properties.hyper3d().apiKey();
        this.api = RestClient.builder()
                .baseUrl(properties.hyper3d().baseUrl() + "/api/v2")
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
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        int index = 0;
        for (String url : photoUrls.stream().limit(MAX_IMAGES).toList()) {
            form.add("images", downloadImage(url, index++));
        }
        form.add("tier", TIER);
        form.add("geometry_file_format", "glb");

        SubmitResponse response = call(() -> api.post().uri("/rodin")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(form)
                .retrieve()
                .body(SubmitResponse.class));
        if (response == null || response.uuid() == null || response.uuid().isBlank()
                || (response.error() != null && !response.error().isBlank())) {
            throw new BusinessRuleException("Hyper3D rejected the task: "
                    + (response == null ? "empty response" : response.error()), HttpStatus.BAD_GATEWAY);
        }
        String subscriptionKey = response.jobs() == null ? null : response.jobs().subscriptionKey();
        return new GenerationTicket(response.uuid(), subscriptionKey);
    }

    @Override
    public GenerationStatus checkStatus(GenerationTicket ticket) {
        requireEnabled();
        if (ticket.subscriptionKey() == null) {
            // Only the task uuid could be stored: the task is done when its GLB can be downloaded.
            return fetchGlbUrl(ticket).isPresent() ? GenerationStatus.COMPLETED : GenerationStatus.PROCESSING;
        }
        StatusResponse response = call(() -> api.post().uri("/status")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new StatusRequest(ticket.subscriptionKey()))
                .retrieve()
                .body(StatusResponse.class));
        List<String> statuses = new ArrayList<>();
        if (response != null && response.jobs() != null) {
            response.jobs().forEach(job -> statuses.add(job.status()));
        }
        return mapStatuses(statuses);
    }

    @Override
    public Optional<String> fetchGlbUrl(GenerationTicket ticket) {
        requireEnabled();
        DownloadResponse response = call(() -> api.post().uri("/download")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new DownloadRequest(ticket.taskUuid()))
                .retrieve()
                .body(DownloadResponse.class));
        if (response == null || response.list() == null) {
            return Optional.empty();
        }
        return response.list().stream()
                .filter(item -> item.name() != null && item.name().toLowerCase(Locale.ROOT).endsWith(".glb"))
                .map(DownloadItem::url)
                .findFirst();
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

    /** Hyper3D receives image files, so each photo URL is downloaded first. */
    private ByteArrayResource downloadImage(String url, int index) {
        if (url == null || !(url.startsWith("http://") || url.startsWith("https://"))) {
            throw BusinessRuleException.unprocessable(
                    "Photo URL must be absolute (http/https) to send it to Hyper3D: " + url);
        }
        byte[] bytes = call(() -> downloader.get().uri(URI.create(url)).retrieve().body(byte[].class));
        if (bytes == null || bytes.length == 0) {
            throw BusinessRuleException.unprocessable("Photo could not be downloaded: " + url);
        }
        String filename = "photo-" + index + extensionOf(url);
        return new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
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

    // ----- Hyper3D JSON formats (only the fields we use) -----

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SubmitResponse(String uuid, SubmitJobs jobs, String error) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SubmitJobs(List<String> uuids, @JsonProperty("subscription_key") String subscriptionKey) {
    }

    record StatusRequest(@JsonProperty("subscription_key") String subscriptionKey) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record StatusResponse(List<StatusJob> jobs) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record StatusJob(String uuid, String status) {
    }

    record DownloadRequest(@JsonProperty("task_uuid") String taskUuid) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record DownloadResponse(List<DownloadItem> list) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record DownloadItem(String url, String name) {
    }
}
