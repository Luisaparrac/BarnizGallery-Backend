package com.barnizgallery.backend.dto;

/**
 * One invalid field of a request body, inside {@link ErrorResponse}.
 */
public record FieldErrorResponse(String field, String message) {
}
