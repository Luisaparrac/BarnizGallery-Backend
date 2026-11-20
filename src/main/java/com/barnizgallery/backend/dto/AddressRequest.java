package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * A shipping address of the logged-in user. The frontend names ({@code line1}, {@code line2}, {@code region},
 * {@code phone}) are accepted as aliases; the first line and a phone are required. {@code countryCode} is ISO 3166-1 alpha-2 (for example CO).
 */
public record AddressRequest(
        @NotBlank @Size(max = 150) String recipientName,
        @NotBlank @Pattern(regexp = "[A-Z]{2}") String countryCode,
        @Size(max = 150) String administrativeArea,
        @NotBlank @Size(max = 150) String city,
        @Size(max = 150) String locality,
        @Size(max = 250) String addressLine1,
        @Size(max = 250) String addressLine2,
        @Size(max = 30) String postalCode,
        @Size(max = 1000) String deliveryInstructions,
        @Size(max = 40) String contactPhone,
        @Size(max = 250) String line1,
        @Size(max = 250) String line2,
        @Size(max = 150) String region,
        @Size(max = 40) String phone) {
}
