package com.barnizgallery.backend.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code app.auction.*} properties.
 *
 * @param currency             single currency of every auction
 * @param minIncrement         minimum amount a bid must add over the current reference
 * @param maxBidsPerMinute     bids allowed per visitor in 60 seconds
 * @param suspiciousMultiplier a bid above this many times the reference is reported as suspicious
 * @param schedulerEnabled     when false the 60-second auction scheduler does not run
 *                             (useful to avoid writes while developing against the real database)
 */
@ConfigurationProperties(prefix = "app.auction")
public record AuctionProperties(String currency, BigDecimal minIncrement, int maxBidsPerMinute,
        BigDecimal suspiciousMultiplier, boolean schedulerEnabled) {
}
