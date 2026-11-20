package com.barnizgallery.backend.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.LocaleResolver;

class LocaleConfigTest {

    private final LocaleResolver resolver = new LocaleConfig().localeResolver();

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
}
