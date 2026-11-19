package com.barnizgallery.backend.enums;

/**
 * Status of an artwork. Stored in {@code artworks.status}.
 */
public enum ArtworkStatus implements DbValueEnum {

    DRAFT("borrador"),
    EXHIBITED("exhibida"),
    IN_AUCTION("subasta"),
    RESERVED("reservada"),
    SOLD("vendida"),
    ARCHIVED("archivada");

    private final String dbValue;

    ArtworkStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
