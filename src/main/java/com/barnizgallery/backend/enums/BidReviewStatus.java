package com.barnizgallery.backend.enums;

/**
 * Review result of a bid. Stored in {@code bids.review_status} (null when not reviewed).
 */
public enum BidReviewStatus implements DbValueEnum {

    VALID("valida"),
    SUSPICIOUS("sospechosa");

    private final String dbValue;

    BidReviewStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String getDbValue() {
        return dbValue;
    }
}
