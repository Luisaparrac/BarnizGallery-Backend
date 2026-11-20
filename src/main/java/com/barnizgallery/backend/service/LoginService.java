package com.barnizgallery.backend.service;

import java.time.Clock;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.AuthResponse;
import com.barnizgallery.backend.dto.LoginRequest;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.UserStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.repository.UserRepository;
import com.barnizgallery.backend.security.LoginAttemptLimiter;

/**
 * Login with email and password.
 * <ul>
 * <li>too many failures for the same email and IP → 429 until the window ends;</li>
 * <li>unknown email, account without password (v1 visitor) or wrong password → the same 401, and the password is
 * always checked against a hash so the answer time does not reveal which case it was;</li>
 * <li>a suspended or deleted account → 403, only after the right password;</li>
 * <li>otherwise {@code last_login_at} is updated and a session is opened.</li>
 * </ul>
 */
@Service
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptLimiter limiter;
    private final SessionIssuer sessionIssuer;
    private final Clock clock;
    private final String dummyHash;

    public LoginService(UserRepository userRepository, PasswordEncoder passwordEncoder, LoginAttemptLimiter limiter,
            SessionIssuer sessionIssuer, Clock clock) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.limiter = limiter;
        this.sessionIssuer = sessionIssuer;
        this.clock = clock;
        this.dummyHash = passwordEncoder.encode("not-a-real-password");
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ip) {
        String email = RegistrationService.normalizeEmail(request.email());
        limiter.checkAllowed(email, ip);
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        String hash = user == null || user.getPasswordHash() == null ? dummyHash : user.getPasswordHash();
        boolean matches = passwordEncoder.matches(request.password(), hash);
        if (user == null || user.getPasswordHash() == null || !matches) {
            limiter.recordFailure(email, ip);
            throw BusinessRuleException.unauthorized(ErrorCode.INVALID_CREDENTIALS);
        }
        limiter.reset(email, ip);
        if (user.getStatus() == UserStatus.SUSPENDED || user.getStatus() == UserStatus.DELETED) {
            throw BusinessRuleException.forbidden(ErrorCode.ACCOUNT_SUSPENDED);
        }
        user.setLastLoginAt(clock.instant());
        return sessionIssuer.open(user);
    }
}
