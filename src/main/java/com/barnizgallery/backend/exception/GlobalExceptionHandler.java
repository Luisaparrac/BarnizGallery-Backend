package com.barnizgallery.backend.exception;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.unit.DataSize;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.barnizgallery.backend.dto.ErrorResponse;
import com.barnizgallery.backend.dto.FieldErrorResponse;
import com.barnizgallery.backend.enums.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Translates exceptions into the common JSON error format:
 * {@code { timestamp, status, error, code, message, path }}. The {@code code} is stable; the {@code message}
 * comes from the message bundles in the language of the request (Spanish or English). Logs stay in English.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messageSource;
    private final long maxUploadMegabytes;

    public GlobalExceptionHandler(MessageSource messageSource,
            @Value("${app.storage.max-upload-size}") DataSize maxUploadSize) {
        this.messageSource = messageSource;
        this.maxUploadMegabytes = maxUploadSize.toMegabytes();
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        return build(ex.getStatus(), ex.getCode(), request, null, ex.getArgs());
    }

    /**
     * Denied by {@code @PreAuthorize}: 401 when nobody is logged in, 403 when the logged-in user lacks permission.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return build(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, request, null);
        }
        return build(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, request, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBody(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        List<FieldErrorResponse> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED, request, fields);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameters(HandlerMethodValidationException ex,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED, request, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.MALFORMED_REQUEST, request, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_PARAMETER, request, null, ex.getName());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_PARAMETER, request, null, ex.getParameterName());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingPart(MissingServletRequestPartException ex,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_PARAMETER, request, null, ex.getRequestPartName());
    }

    /** The multipart request is bigger than the servlet limit (only possible for oversized images). */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleUploadTooLarge(MaxUploadSizeExceededException ex,
            HttpServletRequest request) {
        return build(HttpStatus.CONTENT_TOO_LARGE, ErrorCode.IMAGE_TOO_LARGE, request, null, maxUploadMegabytes);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_ALLOWED, request, null, ex.getMethod());
    }

    /** CHECK / UNIQUE / foreign key violations reported by PostgreSQL. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
            HttpServletRequest request) {
        log.warn("Data integrity violation on {}: {}", request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, ErrorCode.DATA_CONFLICT, request, null);
    }

    /**
     * A business rule raised by a database function or trigger that no service translated first: answered as 409
     * with a generic message, because the database text is only in Spanish.
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccess(DataAccessException ex, HttpServletRequest request) {
        if (DatabaseErrors.isRuleViolation(ex)) {
            log.warn("Database rule rejected {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
            return build(HttpStatus.CONFLICT, ErrorCode.DATABASE_RULE_REJECTED, request, null);
        }
        log.error("Database error on {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error on {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, request, null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, ErrorCode code, HttpServletRequest request,
            List<FieldErrorResponse> fieldErrors, Object... args) {
        String message = messageSource.getMessage(code.messageKey(), args, code.code(),
                LocaleContextHolder.getLocale());
        ErrorResponse body = ErrorResponse.of(status.value(), status.getReasonPhrase(), code.code(), message,
                request.getRequestURI(), fieldErrors);
        return ResponseEntity.status(status).body(body);
    }
}
