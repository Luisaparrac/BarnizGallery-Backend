package com.barnizgallery.backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Common JSON body for every error returned by the API.
 * {@code fieldErrors} is only present when the request body fails validation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldError> fieldErrors) {

    public static ErrorResponse of(int status, String error, String message, String path,
            List<FieldError> fieldErrors) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, fieldErrors);
    }

    /** One invalid field of the request body. */
    public record FieldError(String field, String message) {
    }
}
