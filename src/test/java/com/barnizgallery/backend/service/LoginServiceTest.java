package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.LoginRequest;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.UserStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.repository.UserRepository;
import com.barnizgallery.backend.security.LoginAttemptLimiter;
import com.barnizgallery.backend.security.LoginLimitProperties;

class LoginServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-19T12:00:00Z");
    private static final String IP = "10.0.0.1";

    private final UserRepository userRepository = mock(UserRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final LoginAttemptLimiter limiter = new LoginAttemptLimiter(
            new LoginLimitProperties(3, Duration.ofMinutes(15)), clock);
    private final SessionIssuer sessions = mock(SessionIssuer.class);
    private final LoginService service = new LoginService(userRepository, encoder, limiter, sessions, clock);
    private User user;

    @BeforeEach
    void setUp() {
        user = TestEntities.user(4, Language.ES);
        user.setStatus(UserStatus.ACTIVE);
        user.setPasswordHash(encoder.encode("una-clave-segura"));
        when(userRepository.findByEmailIgnoreCase("user4@test.local")).thenReturn(Optional.of(user));
    }

    @Test
    void rightPasswordOpensASessionAndSavesTheLoginTime() {
        service.login(new LoginRequest(" USER4@test.local ", "una-clave-segura"), IP);

        assertThat(user.getLastLoginAt()).isEqualTo(NOW);
        verify(sessions).open(user);
    }

    @Test
    void wrongPasswordUnknownEmailAndV1UsersGetTheSame401() {
        assertUnauthorized(new LoginRequest("user4@test.local", "otra-clave"));
        assertUnauthorized(new LoginRequest("nobody@test.local", "una-clave-segura"));
        user.setPasswordHash(null);
        assertUnauthorized(new LoginRequest("user4@test.local", "una-clave-segura"));
        verify(sessions, never()).open(user);
    }

    @Test
    void tooManyFailuresBlockEvenTheRightPassword() {
        for (int i = 0; i < 3; i++) {
            assertUnauthorized(new LoginRequest("user4@test.local", "otra-clave"));
        }

        assertThatThrownBy(() -> service.login(new LoginRequest("user4@test.local", "una-clave-segura"), IP))
                .hasMessageStartingWith("too_many_login_attempts");
    }

    @Test
    void suspendedAccountsAre403() {
        user.setStatus(UserStatus.SUSPENDED);

        assertThatThrownBy(() -> service.login(new LoginRequest("user4@test.local", "una-clave-segura"), IP))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN))
                .hasMessage("account_suspended");
    }

    private void assertUnauthorized(LoginRequest request) {
        assertThatThrownBy(() -> service.login(request, IP))
                .isInstanceOfSatisfying(BusinessRuleException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED))
                .hasMessage("invalid_credentials");
    }
}
