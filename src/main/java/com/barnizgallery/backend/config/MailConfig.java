package com.barnizgallery.backend.config;

import java.net.http.HttpClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.barnizgallery.backend.patterns.adapter.BrevoEmailSender;
import com.barnizgallery.backend.patterns.adapter.DisabledEmailSender;
import com.barnizgallery.backend.patterns.adapter.EmailSender;

/**
 * Creates the single {@link EmailSender}: Brevo when its API key and sender are set, otherwise the disabled
 * sender.
 */
@Configuration
public class MailConfig {

    private static final Logger log = LoggerFactory.getLogger(MailConfig.class);

    @Bean
    public EmailSender emailSender(BrevoProperties properties) {
        if (!properties.isConfigured()) {
            log.info("Email disabled: BREVO_API_KEY or MAIL_FROM is empty");
            return new DisabledEmailSender();
        }
        log.info("Email: Brevo");
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(properties.timeout()).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.timeout());
        RestClient restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(BrevoEmailSender.API_KEY_HEADER, properties.apiKey())
                .requestFactory(requestFactory)
                .build();
        return new BrevoEmailSender(restClient, properties);
    }
}
