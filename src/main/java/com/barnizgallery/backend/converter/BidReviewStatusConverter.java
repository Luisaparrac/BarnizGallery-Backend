package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.BidReviewStatus;

import jakarta.persistence.Converter;

/**
 * Converts {@link BidReviewStatus} to its Spanish database value and back.
 */
@Converter
public class BidReviewStatusConverter extends DbValueEnumConverter<BidReviewStatus> {

    public BidReviewStatusConverter() {
        super(BidReviewStatus.class);
    }
}
