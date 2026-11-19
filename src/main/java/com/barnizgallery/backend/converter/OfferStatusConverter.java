package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.OfferStatus;

import jakarta.persistence.Converter;

/**
 * Converts {@link OfferStatus} to its Spanish database value and back.
 */
@Converter
public class OfferStatusConverter extends DbValueEnumConverter<OfferStatus> {

    public OfferStatusConverter() {
        super(OfferStatus.class);
    }
}
