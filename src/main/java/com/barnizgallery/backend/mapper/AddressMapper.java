package com.barnizgallery.backend.mapper;

import com.barnizgallery.backend.dto.AddressRequest;
import com.barnizgallery.backend.dto.AddressResponse;
import com.barnizgallery.backend.entity.Address;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;

/**
 * Converts between {@link Address} and its DTOs.
 */
public final class AddressMapper {

    private AddressMapper() {
    }

    public static AddressResponse toResponse(Address address) {
        return new AddressResponse(address.getAddressId(), address.getRecipientName(), address.getCountryCode(),
                address.getAdministrativeArea(), address.getCity(), address.getLocality(), address.getAddressLine1(),
                address.getAddressLine2(), address.getPostalCode(), address.getDeliveryInstructions(),
                address.getContactPhone(), address.getAddressId(), address.getAddressLine1(), address.getAddressLine2(),
                address.getAdministrativeArea(), address.getContactPhone(), false);
    }

    public static void apply(AddressRequest request, Address address) {
        address.setRecipientName(request.recipientName().trim());
        address.setCountryCode(request.countryCode());
        address.setAdministrativeArea(request.administrativeArea() != null ? request.administrativeArea()
                : request.region());
        address.setCity(request.city().trim());
        address.setLocality(request.locality());
        address.setAddressLine1(first(request.addressLine1(), request.line1()));
        address.setAddressLine2(request.addressLine2() != null ? request.addressLine2() : request.line2());
        address.setPostalCode(request.postalCode());
        address.setDeliveryInstructions(request.deliveryInstructions());
        address.setContactPhone(first(request.contactPhone(), request.phone()));
    }

    /** The first non-blank value, trimmed; 422 when both are missing (the column is required). */
    private static String first(String value, String alias) {
        String chosen = value != null && !value.isBlank() ? value : alias;
        if (chosen == null || chosen.isBlank()) {
            throw BusinessRuleException.unprocessable(ErrorCode.ADDRESS_INCOMPLETE);
        }
        return chosen.trim();
    }
}
