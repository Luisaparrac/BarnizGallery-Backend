package com.barnizgallery.backend.patterns.adapter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Signatures of the Wompi documentation, computed only on the server so the secrets are never exposed.
 * <p>
 * Integrity signature of a checkout: SHA-256 (hex) of
 * {@code <reference><amount in cents><currency>[<expiration time>]<integrity secret>}.
 */
public final class WompiSignatures {

    private WompiSignatures() {
    }

    /**
     * @param expirationTime ISO 8601 expiration of the checkout, or null when it has none
     */
    public static String integrity(String reference, long amountInCents, String currency, String expirationTime,
            String integritySecret) {
        return sha256Hex(reference + amountInCents + currency + (expirationTime == null ? "" : expirationTime)
                + integritySecret);
    }

    /** Wompi amounts go in cents: {@code 24900.00} → {@code 2490000}. */
    public static long amountInCents(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact();
    }

    static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
