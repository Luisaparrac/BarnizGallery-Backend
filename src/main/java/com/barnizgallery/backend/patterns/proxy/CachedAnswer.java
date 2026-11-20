package com.barnizgallery.backend.patterns.proxy;

import java.time.Instant;

/**
 * An answer kept by {@link AiServiceProxy} until {@code validUntil}.
 */
record CachedAnswer(Object answer, Instant validUntil) {
}
