package com.barnizgallery.backend.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;

class OpenApiConfigTest {

    private final OpenApiConfig config = new OpenApiConfig();

    @Test
    void theBearerSchemeIsDeclaredAndOnlyProtectedOperationsRequireIt() {
        OpenAPI openApi = config.barnizGalleryOpenApi();
        assertThat(openApi.getComponents().getSecuritySchemes()).containsKey(OpenApiConfig.BEARER);
        openApi.setPaths(new Paths()
                .addPathItem("/api/cart", new PathItem().get(new Operation()))
                .addPathItem("/api/artworks", new PathItem().get(new Operation()))
                .addPathItem("/api/auth/login", new PathItem().post(new Operation()))
                .addPathItem("/api/artworks/{id}/suggested-price", new PathItem().get(new Operation())));

        config.bearerOnProtectedOperations().customise(openApi);

        assertThat(openApi.getPaths().get("/api/cart").getGet().getSecurity()).isNotEmpty();
        assertThat(openApi.getPaths().get("/api/artworks").getGet().getSecurity()).isNull();
        assertThat(openApi.getPaths().get("/api/auth/login").getPost().getSecurity()).isNull();
        assertThat(openApi.getPaths().get("/api/artworks/{id}/suggested-price").getGet().getSecurity()).isNotEmpty();
    }
}
