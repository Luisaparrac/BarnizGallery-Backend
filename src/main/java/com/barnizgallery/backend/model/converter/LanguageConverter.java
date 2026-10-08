package com.barnizgallery.backend.model.converter;

import com.barnizgallery.backend.model.enums.Language;

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
