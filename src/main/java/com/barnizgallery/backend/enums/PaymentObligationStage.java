package com.barnizgallery.backend.enums;

/**
 * Stage of a payment obligation. Stored in {@code payment_obligations.stage}.
 */
public enum PaymentObligationStage implements DbValueEnum {

    SINGLE_PAYMENT("pago_unico"),
    DEPOSIT("anticipo"),
    BALANCE("saldo");

    private final String dbValue;

    PaymentObligationStage(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
