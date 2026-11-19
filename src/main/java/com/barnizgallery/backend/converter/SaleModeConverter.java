package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.SaleMode;

import jakarta.persistence.Converter;

/**
 * Converts {@link SaleMode} to its Spanish database value and back.
 */
@Converter
public class SaleModeConverter extends DbValueEnumConverter<SaleMode> {

    public SaleModeConverter() {
        super(SaleMode.class);
    }
}
