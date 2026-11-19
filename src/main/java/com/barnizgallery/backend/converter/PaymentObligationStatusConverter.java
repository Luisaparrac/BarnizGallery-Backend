package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.PaymentObligationStatus;

import jakarta.persistence.Converter;

/**
 * Converts {@link PaymentObligationStatus} to its Spanish database value and back.
 */
@Converter
public class PaymentObligationStatusConverter extends DbValueEnumConverter<PaymentObligationStatus> {

    public PaymentObligationStatusConverter() {
        super(PaymentObligationStatus.class);
    }
}
