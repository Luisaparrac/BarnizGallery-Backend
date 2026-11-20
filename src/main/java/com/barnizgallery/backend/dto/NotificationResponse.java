package com.barnizgallery.backend.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * A stored notification, written in the recipient's language. {@code payload} is the extra JSON data, sent as
 * JSON (not as text).
 */
public record NotificationResponse(
        Long notificationId,
        String type,
        String title,
        String body,
        String link,
        @JsonRawValue String payload,
        Instant readAt,
        Instant createdAt,
        Long id,
        String code) {
}
