package com.barnizgallery.backend.exception;

import java.math.BigDecimal;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.PhotoRequest;
import com.barnizgallery.backend.enums.ErrorCode;

import jakarta.validation.Valid;

/**
 * Small controller for {@link GlobalExceptionHandlerTest}: each endpoint throws one kind of error.
 */
@RestController
@Validated
public class ErrorTestController {

    @GetMapping("/not-found")
    public void notFound() {
        throw new ResourceNotFoundException(ErrorCode.ARTWORK_NOT_FOUND, 1234L);
    }

    @GetMapping("/conflict")
    public void conflict() {
        throw new BusinessRuleException(ErrorCode.MASTER_ALREADY_HAS_STORE, 7L);
    }

    @GetMapping("/too-low")
    public void tooLow() {
        throw BusinessRuleException.unprocessable(ErrorCode.BID_TOO_LOW, new BigDecimal("2500000.50"), "COP");
    }

    @GetMapping("/too-many")
    public void tooMany() {
        throw BusinessRuleException.tooManyRequests(ErrorCode.BID_RATE_LIMITED, 5);
    }

    @GetMapping("/disabled")
    public void disabled() {
        throw new FeatureDisabledException(ErrorCode.AI_DISABLED);
    }

    @GetMapping("/denied")
    public void denied() {
        throw new AccessDeniedException("denied by a method rule");
    }

    @GetMapping("/integrity")
    public void integrity() {
        throw new DataIntegrityViolationException("duplicate key");
    }

    @PostMapping("/validate")
    public void validate(@Valid @RequestBody PhotoRequest request) {
        // Only the validation of the body matters here.
    }
}
