package com.barnizgallery.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.AuthResponse;
import com.barnizgallery.backend.dto.EmailTokenRequest;
import com.barnizgallery.backend.dto.ForgotPasswordRequest;
import com.barnizgallery.backend.dto.LoginRequest;
import com.barnizgallery.backend.dto.RefreshTokenRequest;
import com.barnizgallery.backend.dto.RegisterArtisanRequest;
import com.barnizgallery.backend.dto.RegisterRequest;
import com.barnizgallery.backend.dto.ResetPasswordRequest;
import com.barnizgallery.backend.dto.UserResponse;
import com.barnizgallery.backend.security.ClientIpResolver;
import com.barnizgallery.backend.service.ArtisanRegistrationService;
import com.barnizgallery.backend.service.LoginService;
import com.barnizgallery.backend.service.PasswordResetService;
import com.barnizgallery.backend.service.RegistrationService;
import com.barnizgallery.backend.service.SessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * Accounts and sessions: sign-up, login, token refresh, logout, email verification and password reset.
 * These endpoints are public; the others need {@code Authorization: Bearer <accessToken>}.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final RegistrationService registrationService;
    private final LoginService loginService;
    private final SessionService sessionService;
    private final PasswordResetService passwordResetService;
    private final ClientIpResolver clientIpResolver;
    private final ArtisanRegistrationService artisanRegistrationService;

    public AuthController(RegistrationService registrationService, LoginService loginService,
            SessionService sessionService, PasswordResetService passwordResetService,
            ClientIpResolver clientIpResolver, ArtisanRegistrationService artisanRegistrationService) {
        this.registrationService = registrationService;
        this.loginService = loginService;
        this.sessionService = sessionService;
        this.passwordResetService = passwordResetService;
        this.clientIpResolver = clientIpResolver;
        this.artisanRegistrationService = artisanRegistrationService;
    }

    @PostMapping("/register")
    @Operation(summary = "Create a customer account (201) and open a session; a verification email is sent")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationService.register(request));
    }

    @PostMapping("/register-artisan")
    @Operation(summary = "Create a master artisan account (201) with its master profile and verification steps")
    public ResponseEntity<AuthResponse> registerArtisan(@Valid @RequestBody RegisterArtisanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(artisanRegistrationService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Log in with email and password (429 after too many failures)")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return loginService.login(request, clientIpResolver.resolve(http));
    }

    @PostMapping("/refresh")
    @Operation(summary = "New tokens for a refresh token; each refresh token works once")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return sessionService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Close the session of a refresh token")
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        sessionService.logout(request.refreshToken());
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Confirm the email with the token of the verification link")
    public UserResponse verifyEmail(@Valid @RequestBody EmailTokenRequest request) {
        return registrationService.verifyEmail(request.token());
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Send the link to choose a new password (same answer whether the email exists or not)")
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Choose a new password with the token of the link; closes every session")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
    }
}
