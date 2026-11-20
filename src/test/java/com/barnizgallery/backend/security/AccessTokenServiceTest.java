package com.barnizgallery.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.UserRole;

class AccessTokenServiceTest {

    private final JwtConfig jwtConfig = new JwtConfig();
    private final JwtProperties properties = new JwtProperties(randomSecret(), 15, 7);

    @Test
    void tokenCarriesSubjectRoleLanguageAndExpiry() {
        User user = TestEntities.user(42, Language.EN);
        user.setRole(UserRole.ARTISAN);
        IssuedAccessToken issued = service(Clock.systemUTC()).issue(user);

        Jwt jwt = jwtConfig.jwtDecoder(properties).decode(issued.token());

        assertThat(jwt.getSubject()).isEqualTo("42");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo(JwtConfig.ISSUER);
        assertThat(jwt.getClaimAsString(AccessTokenService.ROLE_CLAIM)).isEqualTo("ARTISAN");
        assertThat(jwt.getClaimAsString(AccessTokenService.LANGUAGE_CLAIM)).isEqualTo("en");
        assertThat(jwt.getExpiresAt()).isEqualTo(issued.expiresAt());
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofMinutes(15));
        assertThat(jwt.getHeaders()).containsEntry("alg", "HS256");
    }

    @Test
    void roleClaimBecomesASpringAuthority() {
        String token = service(Clock.systemUTC()).issue(TestEntities.user(1, Language.ES)).token();
        Jwt jwt = jwtConfig.jwtDecoder(properties).decode(token);

        assertThat(SecurityConfig.jwtAuthenticationConverter().convert(jwt).getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_CUSTOMER")
                .noneMatch(authority -> authority.startsWith("ROLE_") && !"ROLE_CUSTOMER".equals(authority));
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        String token = service(Clock.systemUTC()).issue(TestEntities.user(1, Language.ES)).token();
        JwtDecoder otherDecoder = jwtConfig.jwtDecoder(new JwtProperties(randomSecret(), 15, 7));

        assertThatThrownBy(() -> otherDecoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void expiredTokenIsRejected() {
        Clock past = Clock.fixed(Instant.now().minus(Duration.ofHours(2)), ZoneOffset.UTC);
        String token = service(past).issue(TestEntities.user(1, Language.ES)).token();

        assertThatThrownBy(() -> jwtConfig.jwtDecoder(properties).decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void shortOrMissingSecretStopsTheApplication() {
        assertThatThrownBy(() -> new JwtProperties("too-short", 15, 7)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
        assertThatThrownBy(() -> new JwtProperties(null, 15, 7)).isInstanceOf(IllegalStateException.class);
    }

    private AccessTokenService service(Clock clock) {
        return new AccessTokenService(jwtConfig.jwtEncoder(properties), properties, clock);
    }

    private static String randomSecret() {
        return Base64.getEncoder().encodeToString((UUID.randomUUID() + "-" + UUID.randomUUID()).getBytes());
    }
}
