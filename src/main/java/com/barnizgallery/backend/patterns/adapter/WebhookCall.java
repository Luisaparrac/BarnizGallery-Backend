package com.barnizgallery.backend.patterns.adapter;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A webhook call as it arrived: its headers and its raw body (signatures are computed over the exact body).
 *
 * @param headers header names in lower case
 * @param body    raw JSON body
 */
public record WebhookCall(Map<String, String> headers, String body) {

    public WebhookCall {
        headers = headers.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(e -> e.getKey().toLowerCase(Locale.ROOT), Map.Entry::getValue,
                        (first, second) -> first));
    }

    /** Header value by name, ignoring case, or null. */
    public String header(String name) {
        return headers.get(name.toLowerCase(Locale.ROOT));
    }
}
