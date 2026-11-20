package com.barnizgallery.backend.service;

import java.time.Clock;
import java.util.List;
import java.util.Locale;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.NotificationResponse;
import com.barnizgallery.backend.dto.UnreadCountResponse;
import com.barnizgallery.backend.entity.Notification;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.NotificationMapper;
import com.barnizgallery.backend.repository.NotificationRepository;
import com.barnizgallery.backend.security.CurrentUser;

/**
 * Stores notifications for users. The title and the body are written once, in the recipient's preferred
 * language, from the message bundles: {@code notification.<type>.title} and {@code notification.<type>.body}
 * (both receive the same arguments). After the commit, {@link NotificationPusher} also sends it through
 * WebSocket.
 */
@Service
public class NotificationService {

    public static final String WELCOME = "welcome";

    static final int PAGE_SIZE = 100;

    private final NotificationRepository notificationRepository;
    private final MessageSource messageSource;
    private final ApplicationEventPublisher events;
    private final CurrentUser currentUser;
    private final Clock clock;

    public NotificationService(NotificationRepository notificationRepository, MessageSource messageSource,
            ApplicationEventPublisher events, CurrentUser currentUser, Clock clock) {
        this.notificationRepository = notificationRepository;
        this.messageSource = messageSource;
        this.events = events;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    /** The latest {@value #PAGE_SIZE} notifications of the logged-in user, newest first. */
    @Transactional(readOnly = true)
    public List<NotificationResponse> mine(boolean unreadOnly) {
        Long userId = currentUser.requireId();
        PageRequest page = PageRequest.of(0, PAGE_SIZE);
        List<Notification> notifications = unreadOnly
                ? notificationRepository.findByUserUserIdAndReadAtIsNullOrderByCreatedAtDescNotificationIdDesc(userId,
                        page)
                : notificationRepository.findByUserUserIdOrderByCreatedAtDescNotificationIdDesc(userId, page);
        return notifications.stream().map(NotificationMapper::toResponse).toList();
    }

    /** Number of unread notifications of the logged-in user. */
    @Transactional(readOnly = true)
    public UnreadCountResponse unreadCount() {
        return new UnreadCountResponse(notificationRepository.countByUserUserIdAndReadAtIsNull(currentUser.requireId()));
    }

    /** Marks one notification of the logged-in user as read; another user's notification is 404. */
    @Transactional
    public NotificationResponse markRead(Long notificationId) {
        Notification notification = notificationRepository
                .findByNotificationIdAndUserUserId(notificationId, currentUser.requireId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND, notificationId));
        if (notification.getReadAt() == null) {
            notification.setReadAt(clock.instant());
        }
        return NotificationMapper.toResponse(notification);
    }

    /** @return how many notifications were marked as read */
    @Transactional
    public int markAllRead() {
        return notificationRepository.markAllRead(currentUser.requireId(), clock.instant());
    }

    /**
     * @param type    notification type, for example {@value #WELCOME}
     * @param link    frontend path to open, or null
     * @param payload extra data as a JSON object, or null
     * @param args    arguments of the title and body texts
     */
    @Transactional
    public NotificationResponse notify(User user, String type, String link, String payload, Object... args) {
        Locale locale = user.getPreferredLanguage() == Language.EN ? Locale.ENGLISH : Locale.forLanguageTag("es");
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(messageSource.getMessage("notification." + type + ".title", args, locale));
        notification.setBody(messageSource.getMessage("notification." + type + ".body", args, locale));
        notification.setLink(link);
        notification.setPayload(payload);
        NotificationResponse saved = NotificationMapper.toResponse(notificationRepository.save(notification));
        events.publishEvent(new NotificationCreated(user.getUserId(), saved));
        return saved;
    }
}
