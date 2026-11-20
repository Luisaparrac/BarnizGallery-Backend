package com.barnizgallery.backend.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

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

    /** The moment a token created now with this validity expires. */
    public Instant expiry(Duration validity) {
        return clock.instant().plus(validity);
    }
}
