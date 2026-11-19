package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.MasterVerificationStatus;

import jakarta.persistence.Converter;

/**
 * Converts {@link MasterVerificationStatus} to its Spanish database value and back.
 */
@Converter
public class MasterVerificationStatusConverter extends DbValueEnumConverter<MasterVerificationStatus> {

    public MasterVerificationStatusConverter() {
        super(MasterVerificationStatus.class);
    }
}
