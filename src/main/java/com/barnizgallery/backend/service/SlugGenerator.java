package com.barnizgallery.backend.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Turns a name into a URL slug: {@code "Taller de Barniz Ñañez"} → {@code "taller-de-barniz-nanez"}. When the slug
 * is taken, {@code -2}, {@code -3}... is added.
 */
public final class SlugGenerator {

    static final int MAX_LENGTH = 80;

    private SlugGenerator() {
    }

    public static String slugOf(String text) {
        String ascii = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String slug = ascii.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-+|-+$)", "");
        if (slug.length() > MAX_LENGTH) {
            slug = slug.substring(0, MAX_LENGTH).replaceAll("-+$", "");
        }
        return slug.length() < 3 ? "store-" + slug : slug;
    }

    static String unique(String base, Predicate<String> taken) {
        String candidate = base;
        for (int n = 2; taken.test(candidate); n++) {
            candidate = base + "-" + n;
        }
        return candidate;
    }
}
