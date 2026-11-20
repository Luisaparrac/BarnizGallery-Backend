package com.barnizgallery.backend.service;

import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.entity.AuthToken;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.AuthTokenPurpose;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.UserStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.patterns.adapter.EmailSender;
import com.barnizgallery.backend.repository.UserRepository;

/**
 * "Forgot password": an email with a one-time link to choose a new password. It is also how users migrated from
 * v1 (who have no password) create their first one.
 */
@Service
public class PasswordResetService {

    public static final Duration PASSWORD_RESET_VALIDITY = Duration.ofHours(1);

    private final UserRepository userRepository;
    private final AuthTokenService authTokenService;
    private final AccountEmailFactory emails;
    private final EmailSender emailSender;
    private final RegistrationService registrationService;

    public PasswordResetService(UserRepository userRepository, AuthTokenService authTokenService,
            AccountEmailFactory emails, EmailSender emailSender, RegistrationService registrationService) {
        this.userRepository = userRepository;
        this.authTokenService = authTokenService;
        this.emails = emails;
        this.emailSender = emailSender;
        this.registrationService = registrationService;
    }

    /**
     * Sends the reset link when the email belongs to an account that is not deleted. The answer is the same
     * either way, so nobody can find out which emails have an account. Older reset links stop working.
     */
    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmailIgnoreCase(RegistrationService.normalizeEmail(email))
                .filter(user -> user.getStatus() != UserStatus.DELETED)
                .ifPresent(this::sendResetLink);
    }

    /**
     * Sets the new password with the token of the link (used once). Opening the link proves the email, so a
     * pending account becomes active; every open session is closed. A bad link → 400 {@code invalid_email_token}.
     */
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        AuthToken token = authTokenService.find(rawToken, AuthTokenPurpose.PASSWORD_RESET)
                .filter(authTokenService::isUsable)
                .orElseThrow(() -> BusinessRuleException.badRequest(ErrorCode.INVALID_EMAIL_TOKEN));
        String hash = registrationService.encodePassword(newPassword);
        authTokenService.markUsed(token);
        User user = token.getUser();
        user.setPasswordHash(hash);
        registrationService.confirmEmail(user);
        authTokenService.revokeAll(user, AuthTokenPurpose.REFRESH);
    }

    private void sendResetLink(User user) {
        authTokenService.revokeAll(user, AuthTokenPurpose.PASSWORD_RESET);
        String token = authTokenService.create(user, AuthTokenPurpose.PASSWORD_RESET, PASSWORD_RESET_VALIDITY);
        emailSender.send(emails.passwordReset(user, token, PASSWORD_RESET_VALIDITY));
    }
}
