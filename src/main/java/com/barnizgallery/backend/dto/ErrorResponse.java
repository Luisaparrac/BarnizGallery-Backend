package com.barnizgallery.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Common JSON body for every error returned by the API. The frontend decides by {@code code} (stable, for
 * example {@code artwork_not_found}); {@code message} is text for people. {@code fieldErrors} is only present
 * when the request body fails validation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        List<FieldErrorResponse> fieldErrors) {

    public static ErrorResponse of(int status, String error, String code, String message, String path,
            List<FieldErrorResponse> fieldErrors) {
        return new ErrorResponse(LocalDateTime.now(), status, error, code, message, path, fieldErrors);
    }
}
