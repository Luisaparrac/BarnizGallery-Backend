package com.barnizgallery.backend.config;

import java.util.Locale;

import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.security.CurrentUser;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Language of a request: a logged-in user gets their preferred language (saved in the access token);
 * otherwise the {@code Accept-Language} header decides between Spanish and English, with Spanish by default.
 */
public class UserLanguageLocaleResolver extends AcceptHeaderLocaleResolver {

    private final CurrentUser currentUser;

    public UserLanguageLocaleResolver(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        return currentUser.language()
                .map(language -> language == Language.EN ? LocaleConfig.ENGLISH : LocaleConfig.SPANISH)
                .orElseGet(() -> super.resolveLocale(request));
    }
}
