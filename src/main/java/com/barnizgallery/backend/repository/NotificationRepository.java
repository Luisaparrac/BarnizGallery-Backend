package com.barnizgallery.backend.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.entity.Notification;

/**
 * Spring Data repository for {@link Notification}.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Latest notifications of a user, newest first. */
    List<Notification> findByUserUserIdOrderByCreatedAtDescNotificationIdDesc(Long userId, Pageable page);

    /** Latest unread notifications of a user, newest first. */
    List<Notification> findByUserUserIdAndReadAtIsNullOrderByCreatedAtDescNotificationIdDesc(Long userId,
            Pageable page);

    Optional<Notification> findByNotificationIdAndUserUserId(Long notificationId, Long userId);

    @Modifying
    @Query("update Notification n set n.readAt = :readAt where n.user.userId = :userId and n.readAt is null")
    int markAllRead(Long userId, Instant readAt);
}
