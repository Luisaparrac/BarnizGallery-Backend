package com.barnizgallery.backend.security;

import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.UserRole;

/**
 * The user of the current request, read from the access token (no database query). Services use it instead of
 * trusting user ids sent in the request body.
 */
@Component
public class CurrentUser {

    /** Id of the logged-in user, or empty for an anonymous request. */
    public Optional<Long> id() {
        return token().map(jwt -> Long.valueOf(jwt.getSubject()));
    }

    /** Id of the logged-in user; an anonymous request is answered with 401. */
    public Long requireId() {
        return id().orElseThrow(() -> new AccessDeniedException("Login required"));
    }

    /** Role of the logged-in user, or empty for an anonymous request. */
    public Optional<UserRole> role() {
        return token().map(jwt -> UserRole.valueOf(jwt.getClaimAsString(AccessTokenService.ROLE_CLAIM)));
    }

    public boolean hasRole(UserRole role) {
        return role().filter(role::equals).isPresent();
    }

    /** Preferred language saved in the token, or empty for an anonymous request. */
    public Optional<Language> language() {
        return token().map(jwt -> jwt.getClaimAsString(AccessTokenService.LANGUAGE_CLAIM))
                .map(value -> "en".equals(value) ? Language.EN : Language.ES);
    }

    private static Optional<Jwt> token() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return Optional.of(jwtAuthentication.getToken());
        }
        return Optional.empty();
    }
}
