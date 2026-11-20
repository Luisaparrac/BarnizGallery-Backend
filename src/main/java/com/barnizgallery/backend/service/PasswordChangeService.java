package com.barnizgallery.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.AuthTokenPurpose;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.repository.UserRepository;
import com.barnizgallery.backend.security.CurrentUser;

/**
 * The logged-in user changes their password: the current one must match (422 otherwise). Every refresh token is
 * revoked, so other sessions must log in again.
 */
@Service
public class PasswordChangeService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;
    private final CurrentUser currentUser;

    public PasswordChangeService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            AuthTokenService authTokenService, CurrentUser currentUser) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authTokenService = authTokenService;
        this.currentUser = currentUser;
    }

    @Transactional
    public void change(String currentPassword, String newPassword) {
        Long userId = currentUser.requireId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, userId));
        if (user.getPasswordHash() == null || !passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw BusinessRuleException.unprocessable(ErrorCode.WRONG_PASSWORD);
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        authTokenService.revokeAll(user, AuthTokenPurpose.REFRESH);
    }
}
