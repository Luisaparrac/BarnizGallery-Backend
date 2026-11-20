package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.RegisterRequest;
import com.barnizgallery.backend.entity.AuthToken;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.AuthTokenPurpose;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.UserRole;
import com.barnizgallery.backend.enums.UserStatus;
import com.barnizgallery.backend.patterns.adapter.EmailMessage;
import com.barnizgallery.backend.patterns.adapter.EmailSender;
import com.barnizgallery.backend.repository.UserRepository;

class RegistrationServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-19T12:00:00Z");

    private final UserRepository userRepository = mock(UserRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final AuthTokenService tokens = mock(AuthTokenService.class);
    private final AccountEmailFactory emails = mock(AccountEmailFactory.class);
    private final EmailSender emailSender = mock(EmailSender.class);
    private final NotificationService notifications = mock(NotificationService.class);
    private final SessionIssuer sessions = mock(SessionIssuer.class);
    private final RegistrationService service = new RegistrationService(userRepository, encoder, tokens, emails,
            emailSender, notifications, sessions, Clock.fixed(NOW, ZoneOffset.UTC), mock(ApplicationEventPublisher.class));

    @BeforeEach
    void setUp() {
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setUserId(9L);
            return user;
        });
        when(tokens.create(any(), eq(AuthTokenPurpose.EMAIL_VERIFICATION), any())).thenReturn("raw-token");
        when(emails.verification(any(), eq("raw-token"), any()))
                .thenReturn(new EmailMessage("ana@mail.com", "Ana", "s", "<p>h</p>", "t"));
    }

    @Test
    void registerCreatesAPendingCustomerAndSendsTheVerificationEmail() {
        service.register(new RegisterRequest(" Ana ", " Ana@Mail.COM ", "una-clave-segura", "CO", Language.EN));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        User user = saved.getValue();
        assertThat(user.getEmail()).isEqualTo("ana@mail.com");
        assertThat(user.getName()).isEqualTo("Ana");
        assertThat(user.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(user.getPreferredLanguage()).isEqualTo(Language.EN);
        assertThat(user.getPasswordHash()).startsWith("$2a$").doesNotContain("una-clave-segura");
        assertThat(encoder.matches("una-clave-segura", user.getPasswordHash())).isTrue();
        verify(emailSender).send(any(EmailMessage.class));
        verify(notifications).notify(user, NotificationService.WELCOME, "/stores", null, "Ana");
        verify(sessions).open(user);
    }

    @Test
    void anExistingEmailIs409() {
        when(userRepository.findByEmailIgnoreCase("ana@mail.com")).thenReturn(Optional.of(TestEntities.user(1,
                Language.ES)));

        assertThatThrownBy(() -> service.register(new RegisterRequest("Ana", "ana@mail.com", "una-clave-segura", null,
                null))).hasMessage("email_already_registered");
        verify(userRepository, never()).save(any());
    }

    @Test
    void passwordsLongerThan72BytesAreRefused() {
        String password = "ñ".repeat(40);

        assertThatThrownBy(() -> service.encodePassword(password)).hasMessage("password_too_long [72]");
    }

    @Test
    void verifyingTheEmailActivatesThePendingAccount() {
        User user = TestEntities.user(4, Language.ES);
        user.setStatus(UserStatus.PENDING);
        AuthToken token = token(user);
        when(tokens.find("link-token", AuthTokenPurpose.EMAIL_VERIFICATION)).thenReturn(Optional.of(token));
        when(tokens.isUsable(token)).thenReturn(true);

        assertThat(service.verifyEmail("link-token").status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getEmailVerifiedAt()).isEqualTo(NOW);
        verify(tokens).markUsed(token);
    }

    @Test
    void anUsedOrUnknownLinkIs400() {
        when(tokens.find(anyString(), eq(AuthTokenPurpose.EMAIL_VERIFICATION))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verifyEmail("bad")).hasMessage("invalid_email_token");
    }

    static AuthToken token(User user) {
        AuthToken token = new AuthToken();
        token.setUser(user);
        token.setExpiresAt(NOW.plusSeconds(3600));
        return token;
    }
}
