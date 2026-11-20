package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.entity.AuthToken;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.AuthTokenPurpose;
import com.barnizgallery.backend.enums.Language;

class SessionServiceTest {

    private final AuthTokenService tokens = mock(AuthTokenService.class);
    private final SessionIssuer sessions = mock(SessionIssuer.class);
    private final SessionService service = new SessionService(tokens, sessions);
    private final User user = TestEntities.user(4, Language.ES);
    private final AuthToken token = RegistrationServiceTest.token(user);

    @Test
    void aValidRefreshTokenIsUsedOnceAndReplaced() {
        when(tokens.find("refresh", AuthTokenPurpose.REFRESH)).thenReturn(Optional.of(token));
        when(tokens.isUsable(token)).thenReturn(true);

        service.refresh("refresh");

        verify(tokens).markUsed(token);
        verify(sessions).open(user);
    }

    @Test
    void aReusedRefreshTokenClosesEverySession() {
        token.setUsedAt(Instant.parse("2026-11-19T11:00:00Z"));
        when(tokens.find("refresh", AuthTokenPurpose.REFRESH)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.refresh("refresh")).hasMessage("invalid_refresh_token");
        verify(tokens).revokeAll(user, AuthTokenPurpose.REFRESH);
        verify(sessions, never()).open(any());
    }

    @Test
    void unknownOrExpiredRefreshTokensAre401() {
        when(tokens.find("unknown", AuthTokenPurpose.REFRESH)).thenReturn(Optional.empty());
        when(tokens.find("expired", AuthTokenPurpose.REFRESH)).thenReturn(Optional.of(token));
        when(tokens.isUsable(token)).thenReturn(false);

        assertThatThrownBy(() -> service.refresh("unknown")).hasMessage("invalid_refresh_token");
        assertThatThrownBy(() -> service.refresh("expired")).hasMessage("invalid_refresh_token");
        verify(sessions, never()).open(any());
    }

    @Test
    void logoutRevokesTheRefreshTokenOnce() {
        when(tokens.find("refresh", AuthTokenPurpose.REFRESH)).thenReturn(Optional.of(token));

        service.logout("refresh");
        token.setUsedAt(Instant.parse("2026-11-19T11:00:00Z"));
        service.logout("refresh");

        verify(tokens).markUsed(token);
    }
}
