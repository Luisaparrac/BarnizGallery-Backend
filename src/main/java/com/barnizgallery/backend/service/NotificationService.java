package com.barnizgallery.backend.service;

import java.util.Locale;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.NotificationResponse;
import com.barnizgallery.backend.entity.Notification;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.mapper.NotificationMapper;
import com.barnizgallery.backend.repository.NotificationRepository;

/**
 * Stores notifications for users. The title and the body are written once, in the recipient's preferred
 * language, from the message bundles: {@code notification.<type>.title} and {@code notification.<type>.body}
 * (both receive the same arguments). After the commit, {@link NotificationPusher} also sends it through
 * WebSocket.
 */
@Service
public class NotificationService {

    public static final String WELCOME = "welcome";

    private final NotificationRepository notificationRepository;
    private final MessageSource messageSource;
    private final ApplicationEventPublisher events;

    public NotificationService(NotificationRepository notificationRepository, MessageSource messageSource,
            ApplicationEventPublisher events) {
        this.notificationRepository = notificationRepository;
        this.messageSource = messageSource;
        this.events = events;
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
