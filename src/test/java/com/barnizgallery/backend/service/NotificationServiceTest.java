package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.config.MessageConfig;
import com.barnizgallery.backend.dto.NotificationResponse;
import com.barnizgallery.backend.entity.Notification;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.repository.NotificationRepository;
import com.barnizgallery.backend.security.CurrentUser;

class NotificationServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-19T12:00:00Z");

    private final NotificationRepository repository = mock(NotificationRepository.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final NotificationService service = new NotificationService(repository,
            new MessageConfig().messageSource(), events, new CurrentUser(), Clock.fixed(NOW, ZoneOffset.UTC));

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void textsAreWrittenInTheRecipientsLanguageAndAnnounced() {
        when(repository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationResponse spanish = service.notify(TestEntities.user(1, Language.ES), NotificationService.WELCOME,
                "/stores", null, "Ana");
        NotificationResponse english = service.notify(TestEntities.user(2, Language.EN), NotificationService.WELCOME,
                "/stores", null, "John");

        assertThat(spanish.title()).isEqualTo("Bienvenido a MUSEO, Ana");
        assertThat(english.title()).isEqualTo("Welcome to MUSEO, John");
        ArgumentCaptor<NotificationCreated> event = ArgumentCaptor.forClass(NotificationCreated.class);
        verify(events, times(2)).publishEvent(event.capture());
        assertThat(event.getAllValues()).extracting(NotificationCreated::userId).containsExactly(1L, 2L);
    }

    @Test
    void pusherSendsToTheUserTopic() {
        SimpMessageSendingOperations messaging = mock(SimpMessageSendingOperations.class);
        NotificationResponse notification = new NotificationResponse(5L, "welcome", "t", "b", null, null, null, NOW);

        new NotificationPusher(messaging).push(new NotificationCreated(7L, notification));

        verify(messaging).convertAndSend("/topic/users/7/notifications", notification);
    }

    @Test
    void markingAnotherUsersNotificationIs404() {
        login(42);
        when(repository.findByNotificationIdAndUserUserId(9L, 42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markRead(9L)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("notification_not_found [9]");
    }

    @Test
    void markingReadKeepsTheFirstReadTime() {
        login(42);
        Notification notification = new Notification();
        when(repository.findByNotificationIdAndUserUserId(9L, 42L)).thenReturn(Optional.of(notification));

        assertThat(service.markRead(9L).readAt()).isEqualTo(NOW);
    }

    private static void login(long userId) {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject(String.valueOf(userId))
                .claim("role", "CUSTOMER").issuedAt(NOW).expiresAt(NOW.plusSeconds(60)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }
}
