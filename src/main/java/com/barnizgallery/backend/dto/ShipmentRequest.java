package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Carrier and tracking number of a shipped order line.
 */
public record ShipmentRequest(@NotBlank @Size(max = 100) String carrier, @NotBlank @Size(max = 100) String trackingNumber) {
}
