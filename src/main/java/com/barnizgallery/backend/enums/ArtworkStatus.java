package com.barnizgallery.backend.enums;

import java.util.List;

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

    /** Statuses of the artworks the public can see: exhibited or in auction. */
    public static final List<ArtworkStatus> PUBLIC = List.of(EXHIBITED, IN_AUCTION);

    private final String dbValue;

    ArtworkStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
