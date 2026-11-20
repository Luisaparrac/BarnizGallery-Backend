package com.barnizgallery.backend.config;

import java.net.http.HttpClient;
import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.barnizgallery.backend.patterns.adapter.CloudinaryStorageService;
import com.barnizgallery.backend.patterns.adapter.DisabledStorageService;
import com.barnizgallery.backend.patterns.adapter.StorageService;

/**
 * Creates the single {@link StorageService}: Cloudinary when its cloud name, key and secret are set, otherwise
 * the disabled adapter (uploads answer 503 and photos are registered by URL).
 */
@Configuration
public class StorageConfig {

    private static final Logger log = LoggerFactory.getLogger(StorageConfig.class);

    @Bean
    public StorageService storageService(CloudinaryProperties properties, Clock clock) {
        if (!properties.isConfigured()) {
            log.info("Photo storage disabled: the Cloudinary credentials are empty");
            return new DisabledStorageService();
        }
        log.info("Photo storage: Cloudinary");
        return new CloudinaryStorageService(restClient(properties), properties, clock);
    }

    private static RestClient restClient(CloudinaryProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(properties.timeout()).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.timeout());
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }
}
