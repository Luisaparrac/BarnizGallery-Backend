package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.entity.AuthToken;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.AuthTokenPurpose;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.UserStatus;
import com.barnizgallery.backend.patterns.adapter.EmailMessage;
import com.barnizgallery.backend.patterns.adapter.EmailSender;
import com.barnizgallery.backend.repository.UserRepository;

class PasswordResetServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final AuthTokenService tokens = mock(AuthTokenService.class);
    private final AccountEmailFactory emails = mock(AccountEmailFactory.class);
    private final EmailSender emailSender = mock(EmailSender.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final RegistrationService registration = new RegistrationService(userRepository, encoder, tokens,
            emails, emailSender, mock(NotificationService.class), mock(SessionIssuer.class),
            Clock.fixed(Instant.parse("2026-11-19T12:00:00Z"), ZoneOffset.UTC));
    private final PasswordResetService service = new PasswordResetService(userRepository, tokens, emails,
            emailSender, registration);

    @Test
    void unknownEmailsGetTheSameSilentAnswer() {
        when(userRepository.findByEmailIgnoreCase("nobody@test.local")).thenReturn(Optional.empty());

        service.requestReset("nobody@test.local");

        verify(emailSender, never()).send(any());
    }

    @Test
    void knownEmailsReceiveANewLinkAndOlderLinksStop() {
        User user = TestEntities.user(4, Language.ES);
        when(userRepository.findByEmailIgnoreCase("user4@test.local")).thenReturn(Optional.of(user));
        when(tokens.create(eq(user), eq(AuthTokenPurpose.PASSWORD_RESET), any())).thenReturn("raw");
        when(emails.passwordReset(eq(user), eq("raw"), any()))
                .thenReturn(new EmailMessage("user4@test.local", "User 4", "s", "h", "t"));

        service.requestReset(" User4@Test.local ");

        verify(tokens).revokeAll(user, AuthTokenPurpose.PASSWORD_RESET);
        verify(emailSender).send(any(EmailMessage.class));
    }

    @Test
    void aV1UserCreatesTheirPasswordAndTheAccountBecomesActive() {
        User user = TestEntities.user(4, Language.ES);
        user.setStatus(UserStatus.PENDING);
        user.setPasswordHash(null);
        AuthToken token = RegistrationServiceTest.token(user);
        when(tokens.find("raw", AuthTokenPurpose.PASSWORD_RESET)).thenReturn(Optional.of(token));
        when(tokens.isUsable(token)).thenReturn(true);

        service.resetPassword("raw", "mi-nueva-clave");

        assertThat(encoder.matches("mi-nueva-clave", user.getPasswordHash())).isTrue();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getEmailVerifiedAt()).isNotNull();
        verify(tokens).markUsed(token);
        verify(tokens).revokeAll(user, AuthTokenPurpose.REFRESH);
    }

    @Test
    void aBadLinkIs400AndChangesNothing() {
        when(tokens.find("bad", AuthTokenPurpose.PASSWORD_RESET)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword("bad", "mi-nueva-clave")).hasMessage("invalid_email_token");
        verify(tokens, never()).revokeAll(any(), any());
    }
}
