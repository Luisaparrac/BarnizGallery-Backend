package com.barnizgallery.backend.exception;

import org.springframework.http.HttpStatus;

import com.barnizgallery.backend.enums.ErrorCode;

/**
 * Thrown when a requested resource does not exist. Mapped to HTTP 404.
 */
public class ResourceNotFoundException extends ApiException {

    /**
     * @param code for example {@link ErrorCode#ARTWORK_NOT_FOUND}
     * @param id   id of the missing resource (first argument of the message)
     */
    public ResourceNotFoundException(ErrorCode code, Object id) {
        super(code, HttpStatus.NOT_FOUND, id);
    }
}
