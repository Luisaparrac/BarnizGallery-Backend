package com.barnizgallery.backend.model.converter;

import com.barnizgallery.backend.model.enums.ArtworkStatus;

import jakarta.persistence.Converter;

/**
 * Converts {@link ArtworkStatus} to its Spanish database value and back.
 */
@Converter
public class ArtworkStatusConverter extends DbValueEnumConverter<ArtworkStatus> {

    public ArtworkStatusConverter() {
        super(ArtworkStatus.class);
    }
}
