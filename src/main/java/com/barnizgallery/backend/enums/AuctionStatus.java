package com.barnizgallery.backend.enums;

/**
 * Lifecycle status of an auction. Stored in {@code auctions.status}.
 */
public enum AuctionStatus implements DbValueEnum {

    SCHEDULED("programada"),
    ACTIVE("activa"),
    FINISHED("finalizada"),
    CANCELLED("cancelada");

    private final String dbValue;

    AuctionStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
