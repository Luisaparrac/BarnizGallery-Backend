package com.barnizgallery.backend.enums;

/**
 * Status of a payment attempt. Stored in {@code payments.status}.
 */
public enum PaymentStatus implements DbValueEnum {

    PENDING("pendiente"),
    APPROVED("aprobado"),
    DECLINED("rechazado"),
    EXPIRED("expirado"),
    REFUNDED("reembolsado");

    private final String dbValue;

    PaymentStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
