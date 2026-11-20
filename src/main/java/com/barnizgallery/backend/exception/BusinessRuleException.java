package com.barnizgallery.backend.exception;

import org.springframework.http.HttpStatus;

import com.barnizgallery.backend.enums.ErrorCode;

/**
 * Thrown when a request breaks a business rule.
 * By default it is mapped to HTTP 409; some rules use 400, 401, 403, 422 or 429.
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

    /** The caller is not who they say (for example a wrong password): HTTP 401. */
    public static BusinessRuleException unauthorized(ErrorCode code, Object... args) {
        return new BusinessRuleException(HttpStatus.UNAUTHORIZED, code, args);
    }

    /** The caller is known but may not do this (for example a suspended account): HTTP 403. */
    public static BusinessRuleException forbidden(ErrorCode code, Object... args) {
        return new BusinessRuleException(HttpStatus.FORBIDDEN, code, args);
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
