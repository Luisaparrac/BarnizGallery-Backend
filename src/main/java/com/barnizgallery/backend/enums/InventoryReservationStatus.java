package com.barnizgallery.backend.enums;

/**
 * Status of a stock reservation. Stored in {@code inventory_reservations.status}.
 */
public enum InventoryReservationStatus implements DbValueEnum {

    ACTIVE("activa"),
    RELEASED("liberada"),
    CONSUMED("consumida"),
    EXPIRED("expirada");

    private final String dbValue;

    InventoryReservationStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
