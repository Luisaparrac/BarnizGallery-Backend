package com.barnizgallery.backend.dto.response;

/**
 * Status of the backend and of its optional integrations.
 */
public record HealthResponse(
        String status,
        boolean aiEnabled,
        boolean hyper3dEnabled,
        boolean storageEnabled) {
}
