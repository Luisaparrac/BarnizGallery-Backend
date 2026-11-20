package com.barnizgallery.backend.exception;

import org.springframework.http.HttpStatus;

import com.barnizgallery.backend.enums.ErrorCode;

/**
 * Thrown when an optional integration (AI, storage, payments) is not configured or cannot answer.
 * Mapped to HTTP 503.
 */
public class FeatureDisabledException extends ApiException {

    public FeatureDisabledException(ErrorCode code, Object... args) {
        super(code, HttpStatus.SERVICE_UNAVAILABLE, args);
    }
}
