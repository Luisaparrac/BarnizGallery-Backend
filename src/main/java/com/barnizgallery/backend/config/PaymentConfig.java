package com.barnizgallery.backend.config;

import java.net.http.HttpClient;
import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.barnizgallery.backend.patterns.adapter.PayPalApiClient;
import com.barnizgallery.backend.patterns.adapter.PayPalGateway;
import com.barnizgallery.backend.patterns.adapter.PayPalTokenClient;
import com.barnizgallery.backend.patterns.adapter.PayPalWebhookVerifier;
import com.barnizgallery.backend.patterns.adapter.WompiGateway;

/**
 * Creates the real payment gateways. Each one is always created and reports {@code isEnabled() == false}
 * while its credentials are empty, so the registry can tell "not configured" (503) from "currency not
 * supported" (409).
 */
@Configuration
public class PaymentConfig {

    private static final Logger log = LoggerFactory.getLogger(PaymentConfig.class);

    @Bean
    public WompiGateway wompiGateway(WompiProperties properties) {
        log.info("Wompi (COP) {}", properties.isConfigured() ? "enabled" : "disabled: credentials are empty");
        return new WompiGateway(properties);
    }

    @Bean
    public PayPalGateway payPalGateway(PayPalProperties properties, Clock clock) {
        log.info("PayPal (USD) {}", properties.isConfigured() ? "enabled" : "disabled: credentials are empty");
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(properties.timeout()).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.timeout());
        RestClient restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
        PayPalApiClient api = new PayPalApiClient(restClient, new PayPalTokenClient(restClient, properties, clock));
        return new PayPalGateway(api, new PayPalWebhookVerifier(api, properties), properties);
    }
}
