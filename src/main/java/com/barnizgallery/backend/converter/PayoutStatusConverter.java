package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.PayoutStatus;

import jakarta.persistence.Converter;

/**
 * Converts {@link PayoutStatus} to its Spanish database value and back.
 */
@Converter
public class PayoutStatusConverter extends DbValueEnumConverter<PayoutStatus> {

    public PayoutStatusConverter() {
        super(PayoutStatus.class);
    }
}
