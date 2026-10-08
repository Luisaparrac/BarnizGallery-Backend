package com.barnizgallery.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a request breaks a business rule.
 * By default it is mapped to HTTP 409; some rules use 422 or 429.
 */
public class BusinessRuleException extends RuntimeException {

    private final HttpStatus status;

    public BusinessRuleException(String message) {
        this(message, HttpStatus.CONFLICT);
    }

    public BusinessRuleException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /** Business validation of input data (for example an invalid bid amount): HTTP 422. */
    public static BusinessRuleException unprocessable(String message) {
        return new BusinessRuleException(message, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    /** Too many requests in a short time (for example bid flooding): HTTP 429. */
    public static BusinessRuleException tooManyRequests(String message) {
        return new BusinessRuleException(message, HttpStatus.TOO_MANY_REQUESTS);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
