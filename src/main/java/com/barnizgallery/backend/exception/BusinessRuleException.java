package com.barnizgallery.backend.exception;

import org.springframework.http.HttpStatus;

import com.barnizgallery.backend.enums.ErrorCode;

/**
 * Thrown when a request breaks a business rule.
 * By default it is mapped to HTTP 409; some rules use 400, 422 or 429.
 */
public class BusinessRuleException extends ApiException {

    public BusinessRuleException(ErrorCode code, Object... args) {
        super(code, HttpStatus.CONFLICT, args);
    }

    private BusinessRuleException(HttpStatus status, ErrorCode code, Object... args) {
        super(code, status, args);
    }

    /** Invalid request value that is not a field validation (for example an unknown strategy): HTTP 400. */
    public static BusinessRuleException badRequest(ErrorCode code, Object... args) {
        return new BusinessRuleException(HttpStatus.BAD_REQUEST, code, args);
    }

    /** Business validation of input data (for example an invalid bid amount): HTTP 422. */
    public static BusinessRuleException unprocessable(ErrorCode code, Object... args) {
        return new BusinessRuleException(HttpStatus.UNPROCESSABLE_CONTENT, code, args);
    }

    /** Too many requests in a short time (for example bid flooding): HTTP 429. */
    public static BusinessRuleException tooManyRequests(ErrorCode code, Object... args) {
        return new BusinessRuleException(HttpStatus.TOO_MANY_REQUESTS, code, args);
    }
}
