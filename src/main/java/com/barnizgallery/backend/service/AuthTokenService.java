package com.barnizgallery.backend.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.entity.AuthToken;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.AuthTokenPurpose;
import com.barnizgallery.backend.repository.AuthTokenRepository;
import com.barnizgallery.backend.security.OpaqueTokenGenerator;

/**
 * One-time tokens of the accounts (email verification, password reset, refresh). The raw token is returned once
 * to be sent to the user; the table {@code auth_tokens} only keeps its SHA-256 hash.
 */
@Service
public class AuthTokenService {

    private final AuthTokenRepository repository;
    private final OpaqueTokenGenerator generator;
    private final Clock clock;

    public AuthTokenService(AuthTokenRepository repository, OpaqueTokenGenerator generator, Clock clock) {
        this.repository = repository;
        this.generator = generator;
        this.clock = clock;
    }

    /** Creates a token for the user and returns the raw value (never stored). */
    @Transactional
    public String create(User user, AuthTokenPurpose purpose, Duration validity) {
        String raw = generator.generate();
        AuthToken token = new AuthToken();
        token.setUser(user);
        token.setPurpose(purpose);
        token.setTokenHash(generator.hash(raw));
        token.setExpiresAt(expiry(validity));
        repository.save(token);
        return raw;
    }

    /** The stored token for this raw value and purpose, or empty when it does not exist. */
    @Transactional(readOnly = true)
    public Optional<AuthToken> find(String raw, AuthTokenPurpose purpose) {
        return repository.findByHash(generator.hash(raw), purpose);
    }

    /** True when the token was never used and has not expired. */
    public boolean isUsable(AuthToken token) {
        return token.getUsedAt() == null && clock.instant().isBefore(token.getExpiresAt());
    }

    @Transactional
    public void markUsed(AuthToken token) {
        token.setUsedAt(clock.instant());
        repository.save(token);
    }

    /** Revokes every unused token of the user with this purpose (for example all sessions). */
    @Transactional
    public void revokeAll(User user, AuthTokenPurpose purpose) {
        repository.revokeAll(user.getUserId(), purpose, clock.instant());
    }

    /** The moment a token created now with this validity expires. */
    public Instant expiry(Duration validity) {
        return clock.instant().plus(validity);
    }
}
