package com.barnizgallery.backend.patterns.adapter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Signs Cloudinary upload API requests without the SDK, as the Cloudinary documentation describes:
 * the request parameters (except {@code file}, {@code cloud_name}, {@code resource_type} and {@code api_key})
 * are sorted by name, joined as {@code name=value&...}, the API secret is appended and the result is hashed
 * with SHA-1 (hex). The secret itself is never sent.
 */
public class CloudinarySigner {

    private static final Set<String> NOT_SIGNED = Set.of("file", "cloud_name", "resource_type", "api_key");

    private final String apiSecret;

    public CloudinarySigner(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public String sign(Map<String, String> params) {
        String toSign = new TreeMap<>(params).entrySet().stream()
                .filter(e -> !NOT_SIGNED.contains(e.getKey()))
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1")
                    .digest((toSign + apiSecret).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-1 is not available", ex);
        }
    }
}
