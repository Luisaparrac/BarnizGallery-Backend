package com.barnizgallery.backend.dto;

/**
 * A saved shipping address.
 */
public record AddressResponse(
        Long addressId,
        String recipientName,
        String countryCode,
        String administrativeArea,
        String city,
        String locality,
        String addressLine1,
        String addressLine2,
        String postalCode,
        String deliveryInstructions,
        String contactPhone) {
}
