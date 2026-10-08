package com.barnizgallery.backend.exception;

/**
 * Thrown when an optional integration (AI, Hyper3D, storage) is not configured.
 * Mapped to HTTP 503.
 */
public class FeatureDisabledException extends RuntimeException {

    public FeatureDisabledException(String message) {
        super(message);
    }
}
