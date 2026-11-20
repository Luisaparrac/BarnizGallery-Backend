package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.CustomRequestStatus;

import jakarta.persistence.Converter;

/**
 * Converts {@link CustomRequestStatus} to its Spanish database value and back.
 */
@Converter
public class CustomRequestStatusConverter extends DbValueEnumConverter<CustomRequestStatus> {

    public CustomRequestStatusConverter() {
        super(CustomRequestStatus.class);
    }
}
