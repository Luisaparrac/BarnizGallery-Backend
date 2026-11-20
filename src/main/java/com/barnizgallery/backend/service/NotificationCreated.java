package com.barnizgallery.backend.service;

import com.barnizgallery.backend.dto.NotificationResponse;

/**
 * Application event: a notification was stored for a user.
 */
public record NotificationCreated(Long userId, NotificationResponse notification) {
}
