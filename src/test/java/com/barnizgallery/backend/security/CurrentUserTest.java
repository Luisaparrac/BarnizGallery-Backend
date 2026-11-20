package com.barnizgallery.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.UserRole;

class CurrentUserTest {

    private final CurrentUser currentUser = new CurrentUser();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void readsIdRoleAndLanguageFromTheToken() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject("42")
                .claim(AccessTokenService.ROLE_CLAIM, "ADMIN").claim(AccessTokenService.LANGUAGE_CLAIM, "en")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        assertThat(currentUser.requireId()).isEqualTo(42L);
        assertThat(currentUser.role()).contains(UserRole.ADMIN);
        assertThat(currentUser.hasRole(UserRole.ADMIN)).isTrue();
        assertThat(currentUser.hasRole(UserRole.CUSTOMER)).isFalse();
        assertThat(currentUser.language()).contains(Language.EN);
    }

    @Test
    void anonymousRequestHasNoUser() {
        assertThat(currentUser.id()).isEmpty();
        assertThat(currentUser.role()).isEmpty();
        assertThatThrownBy(currentUser::requireId).isInstanceOf(AccessDeniedException.class);
    }
}
