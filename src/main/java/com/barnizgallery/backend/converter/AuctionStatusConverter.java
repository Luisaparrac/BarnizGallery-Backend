package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.AuctionStatus;

import jakarta.persistence.Converter;

/**
 * Converts {@link AuctionStatus} to its Spanish database value and back.
 */
@Converter
public class AuctionStatusConverter extends DbValueEnumConverter<AuctionStatus> {

    public AuctionStatusConverter() {
        super(AuctionStatus.class);
    }
}
