package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.ArtworkStatus;

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
