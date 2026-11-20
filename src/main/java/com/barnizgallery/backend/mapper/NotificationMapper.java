package com.barnizgallery.backend.mapper;

import java.util.Map;

import com.barnizgallery.backend.dto.NotificationResponse;
import com.barnizgallery.backend.entity.Notification;

/**
 * Converts {@link Notification} to its DTO.
 */
public final class NotificationMapper {

    private static final Map<String, String> FRONTEND_TYPES = Map.ofEntries(
            Map.entry("payment_approved", "PAYMENT_APPROVED"),
            Map.entry("payment_declined", "PAYMENT_REJECTED"),
            Map.entry("order_paid", "NEW_ORDER"),
            Map.entry("order_shipped", "ORDER_SHIPPED"),
            Map.entry("new_message", "NEW_MESSAGE"),
            Map.entry("verification_step_approved", "VERIFICATION_APPROVED"),
            Map.entry("artisan_approved", "VERIFICATION_APPROVED"),
            Map.entry("verification_step_rejected", "VERIFICATION_REJECTED"),
            Map.entry("outbid", "OUTBID"),
            Map.entry("matching_auction", "MATCHING_AUCTION"),
            Map.entry("auction_won", "AUCTION_WON"));

    private NotificationMapper() {
    }

    public static NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(notification.getNotificationId(), frontendType(notification.getType()),
                notification.getTitle(), notification.getBody(), notification.getLink(), notification.getPayload(),
                notification.getReadAt(), notification.getCreatedAt(), notification.getNotificationId(),
                notification.getType());
    }

    /**
     * The notification type as the frontend names it ({@code NotificationType}); the stored type stays in
     * {@code code}. Types without a frontend name are {@code SYSTEM}.
     */
    static String frontendType(String stored) {
        return stored == null ? "SYSTEM" : FRONTEND_TYPES.getOrDefault(stored, "SYSTEM");
    }
}
