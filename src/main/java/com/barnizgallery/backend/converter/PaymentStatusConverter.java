package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.PaymentStatus;

import jakarta.persistence.Converter;

/**
 * Converts {@link PaymentStatus} to its Spanish database value and back.
 */
@Converter
public class PaymentStatusConverter extends DbValueEnumConverter<PaymentStatus> {

    public PaymentStatusConverter() {
        super(PaymentStatus.class);
    }
}
