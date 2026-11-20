package com.barnizgallery.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.barnizgallery.backend.MutableClock;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;

class LoginAttemptLimiterTest {

    private static final String IP = "10.0.0.1";

    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-19T12:00:00Z"));
    private final LoginAttemptLimiter limiter = new LoginAttemptLimiter(
            new LoginLimitProperties(3, Duration.ofMinutes(15)), clock);

    @Test
    void blocksAfterTheMaximumFailures() {
        fail(3, "Ana@Mail.com");

        assertThatThrownBy(() -> limiter.checkAllowed("ana@mail.com", IP))
                .isInstanceOfSatisfying(BusinessRuleException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
                    assertThat(ex.getCode()).isEqualTo(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS);
                })
                .hasMessage("too_many_login_attempts [15]");
    }

    @Test
    void allowsFewerFailuresAndOtherIps() {
        fail(2, "ana@mail.com");
        assertThatCode(() -> limiter.checkAllowed("ana@mail.com", IP)).doesNotThrowAnyException();

        fail(1, "ana@mail.com");
        assertThatCode(() -> limiter.checkAllowed("ana@mail.com", "10.0.0.2")).doesNotThrowAnyException();
    }

    @Test
    void unblocksWhenTheWindowEnds() {
        fail(3, "ana@mail.com");
        clock.advance(Duration.ofMinutes(10));
        assertThatThrownBy(() -> limiter.checkAllowed("ana@mail.com", IP)).hasMessage("too_many_login_attempts [5]");

        clock.advance(Duration.ofMinutes(5));
        assertThatCode(() -> limiter.checkAllowed("ana@mail.com", IP)).doesNotThrowAnyException();
    }

    @Test
    void successfulLoginResetsTheCounter() {
        fail(3, "ana@mail.com");
        limiter.reset("ana@mail.com", IP);

        assertThatCode(() -> limiter.checkAllowed("ana@mail.com", IP)).doesNotThrowAnyException();
    }

    private void fail(int times, String email) {
        for (int i = 0; i < times; i++) {
            limiter.recordFailure(email, IP);
        }
    }
}
