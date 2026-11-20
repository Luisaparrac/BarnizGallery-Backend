package com.barnizgallery.backend.enums;

/**
 * Status of a payout to an artisan. Stored in {@code payouts.status}, a text column without a database CHECK;
 * these values are the backend's choice until the database defines them.
 */
public enum PayoutStatus implements DbValueEnum {

    PENDING("pendiente"),
    PAID("pagado");

    private final String dbValue;

    PayoutStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
