package com.barnizgallery.backend.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import com.barnizgallery.backend.dto.ErrorResponse;
import com.barnizgallery.backend.enums.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

/**
 * Writes security errors (401, 403) with the same JSON body as the rest of the API. These errors happen in the
 * security filters, before Spring MVC, so the language is resolved here from the request.
 */
@Component
public class SecurityErrorWriter {

    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;
    private final JsonMapper jsonMapper;

    public SecurityErrorWriter(MessageSource messageSource, LocaleResolver localeResolver, JsonMapper jsonMapper) {
        this.messageSource = messageSource;
        this.localeResolver = localeResolver;
        this.jsonMapper = jsonMapper;
    }

    public void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status, ErrorCode code)
            throws IOException {
        Locale locale = localeResolver.resolveLocale(request);
        String message = messageSource.getMessage(code.messageKey(), null, code.code(), locale);
        ErrorResponse body = ErrorResponse.of(status.value(), status.getReasonPhrase(), code.code(), message,
                request.getRequestURI(), null);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(jsonMapper.writeValueAsString(body));
    }
}
