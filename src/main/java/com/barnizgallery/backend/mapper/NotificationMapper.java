package com.barnizgallery.backend.mapper;

import com.barnizgallery.backend.dto.NotificationResponse;
import com.barnizgallery.backend.entity.Notification;

/**
 * Converts {@link Notification} to its DTO.
 */
public final class NotificationMapper {

    private NotificationMapper() {
    }

    public static NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(notification.getNotificationId(), notification.getType(),
                notification.getTitle(), notification.getBody(), notification.getLink(), notification.getPayload(),
                notification.getReadAt(), notification.getCreatedAt());
    }
}
