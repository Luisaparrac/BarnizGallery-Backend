package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.PaymentObligationStage;

import jakarta.persistence.Converter;

/**
 * Converts {@link PaymentObligationStage} to its Spanish database value and back.
 */
@Converter
public class PaymentObligationStageConverter extends DbValueEnumConverter<PaymentObligationStage> {

    public PaymentObligationStageConverter() {
        super(PaymentObligationStage.class);
    }
}
