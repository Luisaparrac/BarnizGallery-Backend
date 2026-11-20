package com.barnizgallery.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.AuthResponse;
import com.barnizgallery.backend.dto.RegisterArtisanRequest;
import com.barnizgallery.backend.dto.RegisterRequest;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.UserRole;
import com.barnizgallery.backend.exception.BusinessRuleException;

/**
 * {@code POST /api/auth/register} as the frontend uses it: without {@code role} or with {@code CUSTOMER} it creates a
 * customer; with {@code ARTISAN} it creates the artisan with its master profile and verification steps, as
 * {@code /register-artisan} does. Administrators are never created by sign-up (403 {@code forbidden}).
 */
@Service
public class SignUpService {

    private final RegistrationService registrationService;
    private final ArtisanRegistrationService artisanRegistrationService;

    public SignUpService(RegistrationService registrationService,
            ArtisanRegistrationService artisanRegistrationService) {
        this.registrationService = registrationService;
        this.artisanRegistrationService = artisanRegistrationService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.role() == UserRole.ADMIN) {
            throw BusinessRuleException.forbidden(ErrorCode.FORBIDDEN);
        }
        if (request.role() == UserRole.ARTISAN) {
            return artisanRegistrationService.register(new RegisterArtisanRequest(request, request.workshop(),
                    request.city(), null, null, null));
        }
        return registrationService.register(request);
    }
}
