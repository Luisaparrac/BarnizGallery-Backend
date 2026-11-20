package com.barnizgallery.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.enums.Language;

/** Data-dependent rules: own account and own store. */
class AccessRulesTest {

    private final CurrentUser currentUser = new CurrentUser();
    private final AccessGuard access = new AccessGuard(currentUser);
    private final OwnershipGuard ownership = new OwnershipGuard(currentUser);

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usersOnlyReachTheirOwnAccountUnlessAdmin() {
        login(7, "CUSTOMER");
        assertThat(access.isSelf(7L)).isTrue();
        assertThat(access.isSelf(8L)).isFalse();
        assertThat(access.isSelfOrAdmin(8L)).isFalse();

        login(1, "ADMIN");
        assertThat(access.isSelfOrAdmin(8L)).isTrue();
        assertThat(access.isSelf(8L)).isFalse();
    }

    @Test
    void anonymousRequestsAreNeverSelf() {
        assertThat(access.isSelf(7L)).isFalse();
        assertThat(access.isSelfOrAdmin(null)).isFalse();
    }

    @Test
    void artisansOnlyChangeTheirOwnStore() {
        Store own = store(1, 7L);
        Store other = store(2, 8L);
        Store withoutUser = store(3, null);
        login(7, "ARTISAN");

        assertThatCode(() -> ownership.requireStore(own)).doesNotThrowAnyException();
        assertThatThrownBy(() -> ownership.requireStore(other)).hasMessage("forbidden");
        assertThatThrownBy(() -> ownership.requireStore(withoutUser)).hasMessage("forbidden");

        login(1, "ADMIN");
        assertThatCode(() -> ownership.requireStore(other)).doesNotThrowAnyException();
    }

    private static Store store(long id, Long ownerUserId) {
        Master master = TestEntities.master(id);
        if (ownerUserId != null) {
            master.setUser(TestEntities.user(ownerUserId, Language.ES));
        }
        return TestEntities.store(id, master);
    }

    private static void login(long userId, String role) {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject(String.valueOf(userId))
                .claim("role", role).issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }
}
