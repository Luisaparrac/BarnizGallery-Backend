package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.GenerationStatus;

import jakarta.persistence.Converter;

/**
 * Converts {@link GenerationStatus} to its Spanish database value and back.
 */
@Converter
public class GenerationStatusConverter extends DbValueEnumConverter<GenerationStatus> {

    public GenerationStatusConverter() {
        super(GenerationStatus.class);
    }
}
