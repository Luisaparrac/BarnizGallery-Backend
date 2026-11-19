package com.barnizgallery.backend.exception;

/**
 * Error of the separate AI service, with its HTTP status and error {@code code} (for example
 * {@code not_enough_data} or {@code provider_timeout}). Callers decide by the code and always fall back to the
 * fixed rules: it never reaches the API clients.
 */
public class AiServiceException extends RuntimeException {

    private final int status;
    private final String code;

    public AiServiceException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
