package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * An artisan's offer for a custom request ({@code CustomOffer} of the frontend). {@code type} is
 * {@code ACCEPT_PRICE} or {@code COUNTER_OFFER}; {@code status} is {@code SENT}, {@code ACCEPTED}, {@code REJECTED} or
 * {@code EXPIRED} (a withdrawn offer is stored as expired: the database has no withdrawn state).
 */
public record CustomOfferResponse(Long id, Long requestId, Long masterId, String masterName, String storeSlug,
        String type, BigDecimal price, String currency, Integer estimatedDays, String message, Long designId,
        String status, Instant createdAt) {
}
