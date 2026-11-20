package com.barnizgallery.backend.dto;

import java.time.Instant;

import com.barnizgallery.backend.enums.DesignStatus;

/**
 * An AI design of a custom request ({@code CustomDesign} of the frontend).
 */
public record CustomDesignResponse(Long id, String provider, String model, String prompt, DesignStatus status,
        String imageUrl, boolean isSelected, Instant createdAt) {
}
