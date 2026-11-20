package com.barnizgallery.backend.mapper;

import com.barnizgallery.backend.dto.AddressRequest;
import com.barnizgallery.backend.dto.AddressResponse;
import com.barnizgallery.backend.entity.Address;

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
                address.getContactPhone());
    }

    public static void apply(AddressRequest request, Address address) {
        address.setRecipientName(request.recipientName().trim());
        address.setCountryCode(request.countryCode());
        address.setAdministrativeArea(request.administrativeArea());
        address.setCity(request.city().trim());
        address.setLocality(request.locality());
        address.setAddressLine1(request.addressLine1().trim());
        address.setAddressLine2(request.addressLine2());
        address.setPostalCode(request.postalCode());
        address.setDeliveryInstructions(request.deliveryInstructions());
        address.setContactPhone(request.contactPhone().trim());
    }
}
