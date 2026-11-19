package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.VerificationStep;

import jakarta.persistence.Converter;

/**
 * Converts {@link VerificationStep} to its Spanish database value and back.
 */
@Converter
public class VerificationStepConverter extends DbValueEnumConverter<VerificationStep> {

    public VerificationStepConverter() {
        super(VerificationStep.class);
    }
}
