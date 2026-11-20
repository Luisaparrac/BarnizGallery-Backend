package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * A shipping address of the logged-in user. {@code countryCode} is ISO 3166-1 alpha-2 (for example CO).
 */
public record AddressRequest(
        @NotBlank @Size(max = 150) String recipientName,
        @NotBlank @Pattern(regexp = "[A-Z]{2}") String countryCode,
        @Size(max = 150) String administrativeArea,
        @NotBlank @Size(max = 150) String city,
        @Size(max = 150) String locality,
        @NotBlank @Size(max = 250) String addressLine1,
        @Size(max = 250) String addressLine2,
        @Size(max = 30) String postalCode,
        @Size(max = 1000) String deliveryInstructions,
        @NotBlank @Size(max = 40) String contactPhone) {
}
