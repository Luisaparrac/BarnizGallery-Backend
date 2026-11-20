package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.DesignStatus;

import jakarta.persistence.Converter;

/**
 * Converts {@link DesignStatus} to its Spanish database value and back.
 */
@Converter
public class DesignStatusConverter extends DbValueEnumConverter<DesignStatus> {

    public DesignStatusConverter() {
        super(DesignStatus.class);
    }
}
