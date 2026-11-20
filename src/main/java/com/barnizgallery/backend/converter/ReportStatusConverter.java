package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.ReportStatus;

import jakarta.persistence.Converter;

/**
 * Converts {@link ReportStatus} to its Spanish database value and back.
 */
@Converter
public class ReportStatusConverter extends DbValueEnumConverter<ReportStatus> {

    public ReportStatusConverter() {
        super(ReportStatus.class);
    }
}
