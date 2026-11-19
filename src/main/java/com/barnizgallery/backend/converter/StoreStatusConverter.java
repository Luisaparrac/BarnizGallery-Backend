package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.StoreStatus;

import jakarta.persistence.Converter;

/**
 * Converts {@link StoreStatus} to its Spanish database value and back.
 */
@Converter
public class StoreStatusConverter extends DbValueEnumConverter<StoreStatus> {

    public StoreStatusConverter() {
        super(StoreStatus.class);
    }
}
