package com.barnizgallery.backend.config;

import java.util.List;
import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;

import com.barnizgallery.backend.security.CurrentUser;

/**
 * Language of each request. The API speaks Spanish and English: a logged-in user gets their preferred language;
 * otherwise the {@code Accept-Language} header picks one of them (for example {@code en-US} gives English) and
 * anything else, or no header, gives Spanish.
 */
@Configuration
public class LocaleConfig {

    public static final Locale SPANISH = Locale.forLanguageTag("es");
    public static final Locale ENGLISH = Locale.ENGLISH;

    @Bean
    public LocaleResolver localeResolver(CurrentUser currentUser) {
        UserLanguageLocaleResolver resolver = new UserLanguageLocaleResolver(currentUser);
        resolver.setSupportedLocales(List.of(SPANISH, ENGLISH));
        resolver.setDefaultLocale(SPANISH);
        return resolver;
    }
}
