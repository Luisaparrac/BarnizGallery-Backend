package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.AuthTokenPurpose;

import jakarta.persistence.Converter;

/**
 * Converts {@link AuthTokenPurpose} to its Spanish database value and back.
 */
@Converter
public class AuthTokenPurposeConverter extends DbValueEnumConverter<AuthTokenPurpose> {

    public AuthTokenPurposeConverter() {
        super(AuthTokenPurpose.class);
    }
}
