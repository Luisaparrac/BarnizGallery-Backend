package com.barnizgallery.backend.exception;

import org.springframework.http.HttpStatus;

import com.barnizgallery.backend.enums.ErrorCode;

/**
 * Thrown when an optional integration (AI, storage) is not configured or cannot answer.
 * Mapped to HTTP 503.
 */
public class FeatureDisabledException extends ApiException {

    public FeatureDisabledException(ErrorCode code) {
        super(code, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
