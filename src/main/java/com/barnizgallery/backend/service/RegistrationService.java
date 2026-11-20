package com.barnizgallery.backend.service;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.AuthResponse;
import com.barnizgallery.backend.dto.RegisterRequest;
import com.barnizgallery.backend.dto.UserResponse;
import com.barnizgallery.backend.entity.AuthToken;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.AuthTokenPurpose;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.UserRole;
import com.barnizgallery.backend.enums.UserStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.mapper.UserMapper;
import com.barnizgallery.backend.patterns.adapter.EmailSender;
import com.barnizgallery.backend.repository.UserRepository;

/**
 * Sign-up of customer accounts. A new account starts {@code pendiente} until its email is confirmed; it can log
 * in meanwhile. An email that already exists (also a v1 visitor without password) is refused: those users create
 * their password with "forgot password".
 */
@Service
public class RegistrationService {

    public static final Duration EMAIL_VERIFICATION_VALIDITY = Duration.ofHours(24);
    static final int MAX_PASSWORD_BYTES = 72;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;
    private final AccountEmailFactory emails;
    private final EmailSender emailSender;
    private final NotificationService notificationService;
    private final SessionIssuer sessionIssuer;
    private final Clock clock;

    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            AuthTokenService authTokenService, AccountEmailFactory emails, EmailSender emailSender,
            NotificationService notificationService, SessionIssuer sessionIssuer, Clock clock) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authTokenService = authTokenService;
        this.emails = emails;
        this.emailSender = emailSender;
        this.notificationService = notificationService;
        this.sessionIssuer = sessionIssuer;
        this.clock = clock;
    }

    /** Creates the customer, sends the verification email, greets the user and opens a session. */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        User saved = createAccount(request, UserRole.CUSTOMER);
        notificationService.notify(saved, NotificationService.WELCOME, "/stores", null, saved.getName());
        return sessionIssuer.open(saved);
    }

    /**
     * Creates a {@code pendiente} account with this role and sends the email verification link. A used email → 409.
     */
    @Transactional
    public User createAccount(RegisterRequest request, UserRole role) {
        String email = normalizeEmail(request.email());
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new BusinessRuleException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(encodePassword(request.password()));
        user.setRole(role);
        user.setStatus(UserStatus.PENDING);
        user.setCountryCode(request.countryCode());
        user.setPreferredLanguage(request.preferredLanguage() != null ? request.preferredLanguage()
                : requestLanguage());
        User saved = userRepository.save(user);

        String token = authTokenService.create(saved, AuthTokenPurpose.EMAIL_VERIFICATION,
                EMAIL_VERIFICATION_VALIDITY);
        emailSender.send(emails.verification(saved, token, EMAIL_VERIFICATION_VALIDITY));
        return saved;
    }

    /**
     * Confirms the email with the token of the verification link (used once): the account becomes
     * {@code activo}. An unknown, used or expired link → 400 {@code invalid_email_token}.
     */
    @Transactional
    public UserResponse verifyEmail(String rawToken) {
        AuthToken token = authTokenService.find(rawToken, AuthTokenPurpose.EMAIL_VERIFICATION)
                .filter(authTokenService::isUsable)
                .orElseThrow(() -> BusinessRuleException.badRequest(ErrorCode.INVALID_EMAIL_TOKEN));
        authTokenService.markUsed(token);
        User user = token.getUser();
        confirmEmail(user);
        return UserMapper.toResponse(user);
    }

    /** Marks the email as verified; a pending account becomes active. Suspended accounts stay suspended. */
    void confirmEmail(User user) {
        if (user.getEmailVerifiedAt() == null) {
            user.setEmailVerifiedAt(clock.instant());
        }
        if (user.getStatus() == UserStatus.PENDING) {
            user.setStatus(UserStatus.ACTIVE);
        }
    }

    /** BCrypt hash of a password; BCrypt only reads 72 bytes, so longer passwords are refused. */
    public String encodePassword(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw BusinessRuleException.unprocessable(ErrorCode.PASSWORD_TOO_LONG, MAX_PASSWORD_BYTES);
        }
        return passwordEncoder.encode(password);
    }

    static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static Language requestLanguage() {
        return "en".equals(LocaleContextHolder.getLocale().getLanguage()) ? Language.EN : Language.ES;
    }
}
