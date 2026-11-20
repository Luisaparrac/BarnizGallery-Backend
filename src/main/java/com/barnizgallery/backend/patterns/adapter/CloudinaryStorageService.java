package com.barnizgallery.backend.patterns.adapter;

import java.io.IOException;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import com.barnizgallery.backend.config.CloudinaryProperties;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.FeatureDisabledException;

/**
 * <b>Adapter pattern – Adapter.</b>
 * <p>
 * Stores images in Cloudinary through its REST API, without the SDK: each request is signed with
 * {@link CloudinarySigner} and the answer is translated into a {@link StoredImage}.
 * <ul>
 * <li>upload: {@code POST /v1_1/{cloud}/image/upload} (multipart) with the folder and the timestamp signed;</li>
 * <li>delete: {@code POST /v1_1/{cloud}/image/destroy} with the {@code public_id}; an image that is already gone
 * counts as deleted.</li>
 * </ul>
 * A failure of Cloudinary is answered with 503 {@code storage_unavailable}; only the HTTP status is logged,
 * never the credentials.
 */
public class CloudinaryStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryStorageService.class);

    private final RestClient restClient;
    private final CloudinaryProperties properties;
    private final CloudinarySigner signer;
    private final Clock clock;

    public CloudinaryStorageService(RestClient restClient, CloudinaryProperties properties, Clock clock) {
        this.restClient = restClient;
        this.properties = properties;
        this.signer = new CloudinarySigner(properties.apiSecret());
        this.clock = clock;
    }

    @Override
    public StoredImage upload(MultipartFile file, String folder) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("folder", properties.folder() + "/" + folder);
        params.put("timestamp", timestamp());

        MultipartBodyBuilder body = new MultipartBodyBuilder();
        body.part("file", bytes(file))
                .filename(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename())
                .contentType(MediaType.parseMediaType(file.getContentType() == null
                        ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType()));
        params.forEach(body::part);
        body.part("api_key", properties.apiKey());
        body.part("signature", signer.sign(params));

        CloudinaryUploadResponse answer = call("upload", () -> restClient.post()
                .uri("/v1_1/{cloud}/image/upload", properties.cloudName())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body.build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw failure("upload", response.getStatusCode().value());
                })
                .body(CloudinaryUploadResponse.class));
        if (answer == null || answer.publicId() == null || answer.secureUrl() == null) {
            throw failure("upload", 200);
        }
        return new StoredImage(answer.publicId(), answer.secureUrl(), answer.width(), answer.height());
    }

    @Override
    public void delete(String storageKey) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("public_id", storageKey);
        params.put("timestamp", timestamp());

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        params.forEach(form::add);
        form.add("api_key", properties.apiKey());
        form.add("signature", signer.sign(params));

        CloudinaryDestroyResponse answer = call("delete", () -> restClient.post()
                .uri("/v1_1/{cloud}/image/destroy", properties.cloudName())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw failure("delete", response.getStatusCode().value());
                })
                .body(CloudinaryDestroyResponse.class));
        String result = answer == null ? null : answer.result();
        if (!"ok".equals(result) && !"not found".equals(result)) {
            throw failure("delete", 200);
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private String timestamp() {
        return String.valueOf(clock.instant().getEpochSecond());
    }

    private static <T> T call(String operation, Supplier<T> request) {
        try {
            return request.get();
        } catch (RestClientException ex) {
            log.warn("Cloudinary {} did not answer: {}", operation, ex.getClass().getSimpleName());
            throw new FeatureDisabledException(ErrorCode.STORAGE_UNAVAILABLE);
        }
    }

    private static FeatureDisabledException failure(String operation, int status) {
        log.warn("Cloudinary {} failed with HTTP {}", operation, status);
        return new FeatureDisabledException(ErrorCode.STORAGE_UNAVAILABLE);
    }

    private static byte[] bytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new FeatureDisabledException(ErrorCode.STORAGE_UNAVAILABLE);
        }
    }
}
