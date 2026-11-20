package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.FulfillmentStatus;

import jakarta.persistence.Converter;

/**
 * Converts {@link FulfillmentStatus} to its Spanish database value and back.
 */
@Converter
public class FulfillmentStatusConverter extends DbValueEnumConverter<FulfillmentStatus> {

    public FulfillmentStatusConverter() {
        super(FulfillmentStatus.class);
    }
}
