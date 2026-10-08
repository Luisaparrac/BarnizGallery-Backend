package com.barnizgallery.backend.enums;

/**
 * Status of an artwork. Stored in {@code artworks.status}.
 */
public enum ArtworkStatus implements DbValueEnum {

    EXHIBITED("exhibida"),
    IN_AUCTION("subasta"),
    SOLD("vendida");

    private final String dbValue;

    ArtworkStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
