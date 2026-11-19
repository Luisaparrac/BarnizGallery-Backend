package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.OfferType;

import jakarta.persistence.Converter;

/**
 * Converts {@link OfferType} to its Spanish database value and back.
 */
@Converter
public class OfferTypeConverter extends DbValueEnumConverter<OfferType> {

    public OfferTypeConverter() {
        super(OfferType.class);
    }
}
