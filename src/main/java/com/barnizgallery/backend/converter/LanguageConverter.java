package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.Language;

import jakarta.persistence.Converter;

/**
 * Converts {@link Language} to its Spanish database value and back.
 */
@Converter
public class LanguageConverter extends DbValueEnumConverter<Language> {

    public LanguageConverter() {
        super(Language.class);
    }
}
