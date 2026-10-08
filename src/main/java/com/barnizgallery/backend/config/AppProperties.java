package com.barnizgallery.backend.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed view of the {@code app.*} properties defined in application.properties.
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(Cors cors, Auction auction, Ai ai, Hyper3d hyper3d, Storage storage) {

    public record Cors(List<String> allowedOrigins) {
    }

    public record Auction(String currency, BigDecimal minIncrement, int maxBidsPerMinute,
            BigDecimal suspiciousMultiplier) {
    }

    public record Ai(String provider, String apiKey) {
    }

    public record Hyper3d(String apiKey, String baseUrl) {
    }

    public record Storage(String provider) {
    }
}
