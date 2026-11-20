package com.barnizgallery.backend.exception;

import java.util.Arrays;

import org.springframework.http.HttpStatus;

import com.barnizgallery.backend.enums.ErrorCode;

/**
 * Base of the errors the API answers on purpose: an HTTP status, a stable {@link ErrorCode} and the arguments of
 * its text. The text for the user is resolved later, in the language of the request; {@link #getMessage()} is a
 * short technical text for logs and tests (code and arguments).
 */
public abstract class ApiException extends RuntimeException {

    private final ErrorCode code;
    private final HttpStatus status;
    private final transient Object[] args;

    protected ApiException(ErrorCode code, HttpStatus status, Object... args) {
        super(code.code() + (args.length == 0 ? "" : " " + Arrays.toString(args)));
        this.code = code;
        this.status = status;
        this.args = Arrays.stream(args).map(ApiException::plain).toArray();
    }

    public ErrorCode getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    /** Arguments of the message text. Whole numbers (ids) are kept as text so they are not formatted. */
    public Object[] getArgs() {
        return args.clone();
    }

    private static Object plain(Object arg) {
        return arg instanceof Long || arg instanceof Integer ? arg.toString() : arg;
    }
}
