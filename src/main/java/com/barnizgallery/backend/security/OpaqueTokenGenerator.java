package com.barnizgallery.backend.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.stereotype.Component;

/**
 * Random opaque tokens (refresh tokens and the links of the account emails). The token is given to the user
 * once; the database only keeps its SHA-256 hash ({@code auth_tokens.token_hash}), so a leaked table does not
 * give usable tokens.
 */
@Component
public class OpaqueTokenGenerator {

    static final int TOKEN_BYTES = 32;

    private final SecureRandom random = new SecureRandom();

    /** A new random token: {@value #TOKEN_BYTES} bytes in URL-safe Base64 without padding. */
    public String generate() {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** SHA-256 of the token, in lowercase hex: the value stored and searched in the database. */
    public String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
