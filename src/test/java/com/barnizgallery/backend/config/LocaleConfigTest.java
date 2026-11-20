package com.barnizgallery.backend.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.servlet.LocaleResolver;

import com.barnizgallery.backend.security.CurrentUser;

class LocaleConfigTest {

    private final LocaleResolver resolver = new LocaleConfig().localeResolver(new CurrentUser());

    @Test
    void spanishIsTheDefault() {
        assertThat(resolve(null).getLanguage()).isEqualTo("es");
    }

    @Test
    void acceptLanguagePicksSpanishOrEnglish() {
        assertThat(resolve("en-US,en;q=0.9").getLanguage()).isEqualTo("en");
        assertThat(resolve("es-CO").getLanguage()).isEqualTo("es");
    }

    @Test
    void unsupportedLanguagesFallBackToSpanish() {
        assertThat(resolve("fr-FR").getLanguage()).isEqualTo("es");
    }

    private Locale resolve(String header) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (header != null) {
            request.addHeader("Accept-Language", header);
        }
        return resolver.resolveLocale(request);
    }

    @Test
    void loggedInUserGetsTheirPreferredLanguage() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject("1").claim("role", "CUSTOMER")
                .claim("lang", "en").issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        try {
            assertThat(resolve("es-CO").getLanguage()).isEqualTo("en");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
