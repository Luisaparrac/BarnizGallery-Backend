package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.VerificationStepStatus;

import jakarta.persistence.Converter;

/**
 * Converts {@link VerificationStepStatus} to its Spanish database value and back.
 */
@Converter
public class VerificationStepStatusConverter extends DbValueEnumConverter<VerificationStepStatus> {

    public VerificationStepStatusConverter() {
        super(VerificationStepStatus.class);
    }
}
