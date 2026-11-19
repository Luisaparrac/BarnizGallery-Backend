package com.barnizgallery.backend.enums;

/**
 * Status of a payment obligation. Stored in {@code payment_obligations.status}.
 */
public enum PaymentObligationStatus implements DbValueEnum {

    PENDING("pendiente"),
    PAID("pagada"),
    CANCELLED("cancelada"),
    OVERDUE("vencida");

    private final String dbValue;

    PaymentObligationStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
