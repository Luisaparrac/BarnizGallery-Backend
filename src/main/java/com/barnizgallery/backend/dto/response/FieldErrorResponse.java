package com.barnizgallery.backend.dto.response;

/**
 * One invalid field of a request body, inside {@link ErrorResponse}.
 */
public record FieldErrorResponse(String field, String message) {
}
