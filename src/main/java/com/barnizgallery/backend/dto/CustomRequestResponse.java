package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.barnizgallery.backend.enums.CustomRequestStatus;

/**
 * A custom request as the frontend reads it ({@code CustomRequest}). {@code status} uses the frontend names
 * ({@code DRAFT}, {@code PUBLISHED}, {@code WITH_OFFERS}, {@code ACCEPTED}, {@code IN_PRODUCTION}, {@code DELIVERED},
 * {@code CANCELLED}, {@code EXPIRED}); {@code state} is the approved status stored in the database.
 */
public record CustomRequestResponse(
        Long id,
        Long buyerId,
        String buyerName,
        Long targetMasterId,
        String title,
        String description,
        Long shapeId,
        Long styleId,
        List<Long> colorIds,
        List<Long> motifIds,
        String sizeNotes,
        BigDecimal offeredPrice,
        String currency,
        Instant desiredDate,
        String status,
        CustomRequestStatus state,
        Long acceptedOfferId,
        List<CustomDesignResponse> designs,
        List<CustomOfferResponse> offers,
        Instant expiresAt,
        Instant createdAt,
        String declineReason) {
}
