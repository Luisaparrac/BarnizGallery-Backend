package com.barnizgallery.backend.config;

import java.util.List;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Metadata shown in Swagger UI ({@code /swagger-ui.html}) and the Bearer JWT scheme behind its Authorize button.
 * Every operation needs the access token except the public ones (the same rules as the security filter chain).
 */
@Configuration
public class OpenApiConfig {

    static final String BEARER = "bearerAuth";

    /** Public in every method. */
    private static final List<String> PUBLIC_PATHS = List.of("/api/auth/login", "/api/auth/register",
            "/api/auth/register-artisan", "/api/auth/refresh", "/api/auth/logout", "/api/auth/verify-email",
            "/api/auth/forgot-password", "/api/auth/reset-password", "/api/payments/webhooks/*",
            "/api/recommendations/preview");

    /** Public for GET only. */
    private static final List<String> PUBLIC_READS = List.of("/api/health", "/api/gallery/**", "/api/stores/**",
            "/api/masters/**", "/api/artworks/**", "/api/auctions/**", "/api/catalogs/**", "/api/settings/public");

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    @Bean
    public OpenAPI barnizGalleryOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Barniz Gallery API")
                        .version("v1")
                        .description("REST API of MUSEO, the gallery and shop of Barniz de Pasto Mopa-Mopa. "
                                + "Log in with POST /api/auth/login and paste the accessToken in Authorize."))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Access token of POST /api/auth/login (valid JWT_ACCESS_MINUTES minutes)")));
    }

    /** Marks every protected operation with the Bearer requirement, so Swagger UI sends the token. */
    @Bean
    public OpenApiCustomizer bearerOnProtectedOperations() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().forEach((path, item) -> item.readOperationsMap().forEach((method, operation) -> {
                if (!isPublic(path, method)) {
                    operation.addSecurityItem(new SecurityRequirement().addList(BEARER));
                }
            }));
        };
    }

    static boolean isPublic(String path, PathItem.HttpMethod method) {
        if (PUBLIC_PATHS.stream().anyMatch(pattern -> MATCHER.match(pattern, path))) {
            return true;
        }
        return method == PathItem.HttpMethod.GET && !path.endsWith("/suggested-price")
                && PUBLIC_READS.stream().anyMatch(pattern -> MATCHER.match(pattern, path));
    }
}
