package com.barnizgallery.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Metadata shown in Swagger UI ({@code /swagger-ui.html}).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI barnizGalleryOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Barniz Gallery API")
                .version("v1")
                .description("REST API of MUSEO, the immersive 3D gallery of Barniz de Pasto Mopa-Mopa."));
    }
}
