package com.barnizgallery.backend.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;

/**
 * Slows down password guessing: after {@code maxAttempts} failed logins for the same email and IP inside the
 * window, more logins are refused with 429 until the window ends. Kept in memory (one instance on Render), so a
 * restart forgets the counters.
 */
@Component
public class LoginAttemptLimiter {

    private final LoginLimitProperties properties;
    private final Clock clock;
    private final Map<String, LoginAttemptWindow> windows = new ConcurrentHashMap<>();

    public LoginAttemptLimiter(LoginLimitProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    /** Fails with 429 {@code too_many_login_attempts} while the email and IP are blocked. */
    public void checkAllowed(String email, String ip) {
        Instant now = clock.instant();
        LoginAttemptWindow window = windows.get(key(email, ip));
        if (window == null || expired(window, now) || window.failures() < properties.maxAttempts()) {
            return;
        }
        Duration left = Duration.between(now, window.startedAt().plus(properties.window()));
        long minutes = Math.max(1, (left.toSeconds() + 59) / 60);
        throw BusinessRuleException.tooManyRequests(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS, minutes);
    }

    public void recordFailure(String email, String ip) {
        Instant now = clock.instant();
        windows.values().removeIf(window -> expired(window, now));
        windows.merge(key(email, ip), new LoginAttemptWindow(1, now), (old, added) -> old.addFailure());
    }

    /** Forgets the failures after a successful login. */
    public void reset(String email, String ip) {
        windows.remove(key(email, ip));
    }

    private boolean expired(LoginAttemptWindow window, Instant now) {
        return !now.isBefore(window.startedAt().plus(properties.window()));
    }

    private static String key(String email, String ip) {
        return (email == null ? "" : email.trim().toLowerCase(Locale.ROOT)) + "|" + ip;
    }
}
