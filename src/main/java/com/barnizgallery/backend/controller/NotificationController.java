package com.barnizgallery.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.NotificationResponse;
import com.barnizgallery.backend.dto.UnreadCountResponse;
import com.barnizgallery.backend.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Notifications of the logged-in user (the user comes from the access token, never from the request).
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Latest notifications of the logged-in user, newest first")
    public List<NotificationResponse> mine(@RequestParam(defaultValue = "false") boolean unreadOnly) {
        return notificationService.mine(unreadOnly);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "{ count } of unread notifications of the logged-in user")
    public UnreadCountResponse unreadCount() {
        return notificationService.unreadCount();
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark one notification as read")
    public NotificationResponse markRead(@PathVariable Long id) {
        return notificationService.markRead(id);
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark every notification as read; returns how many changed")
    public Map<String, Integer> markAllRead() {
        return Map.of("updated", notificationService.markAllRead());
    }
}
