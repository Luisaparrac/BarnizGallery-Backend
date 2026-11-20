package com.barnizgallery.backend.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.patterns.adapter.EmailMessage;

/**
 * Writes the account emails in the user's preferred language. The texts come from the message bundles
 * ({@code mail.*} keys); the links point to the frontend ({@code FRONTEND_URL}) and carry the one-time token.
 * Every value put into the HTML is escaped.
 */
@Component
public class AccountEmailFactory {

    private final MessageSource messageSource;
    private final String frontendUrl;

    public AccountEmailFactory(MessageSource messageSource, @Value("${app.frontend-url}") String frontendUrl) {
        this.messageSource = messageSource;
        this.frontendUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1)
                : frontendUrl;
    }

    /** Email with the link that confirms the address of a new account. */
    public EmailMessage verification(User user, String token, Duration validity) {
        return build(user, "mail.verify", "/verify-email?token=" + encode(token), validity);
    }

    private EmailMessage build(User user, String prefix, String path, Duration validity) {
        Locale locale = locale(user);
        String link = frontendUrl + path;
        String subject = text(prefix + ".subject", locale);
        String greeting = text("mail.greeting", locale, user.getName());
        String intro = text(prefix + ".intro", locale);
        String button = text(prefix + ".button", locale);
        String expiry = text("mail.expiry", locale, validity.toHours());
        String ignore = text(prefix + ".ignore", locale);

        String html = """
                <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;color:#2b2118">
                <h2 style="color:#8a3b12">MUSEO · Barniz de Pasto</h2>
                <p>%s</p><p>%s</p>
                <p><a href="%s" style="background:#8a3b12;color:#fff;padding:12px 20px;border-radius:6px;\
                text-decoration:none;display:inline-block">%s</a></p>
                <p style="font-size:13px">%s</p><p style="font-size:13px;color:#6b5d50">%s</p>
                </div>""".formatted(escape(greeting), escape(intro), escape(link), escape(button), escape(expiry),
                escape(ignore));
        String text = String.join("\n\n", greeting, intro, button + ": " + link, expiry, ignore);
        return new EmailMessage(user.getEmail(), user.getName(), subject, html, text);
    }

    private String text(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, locale);
    }

    private static Locale locale(User user) {
        return user.getPreferredLanguage() == Language.EN ? Locale.ENGLISH : Locale.forLanguageTag("es");
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return HtmlUtils.htmlEscape(value, StandardCharsets.UTF_8.name());
    }
}
