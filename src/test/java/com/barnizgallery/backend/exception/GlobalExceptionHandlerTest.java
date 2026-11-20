package com.barnizgallery.backend.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.barnizgallery.backend.config.LocaleConfig;
import com.barnizgallery.backend.config.MessageConfig;

/**
 * Checks the JSON error format with a small controller (no Spring context, no database).
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ErrorTestController())
                .setControllerAdvice(new GlobalExceptionHandler(new MessageConfig().messageSource()))
                .setLocaleResolver(new LocaleConfig().localeResolver())
                .build();
    }

    @Test
    void notFoundIs404WithCodeMessageAndPath() throws Exception {
        mockMvc.perform(get("/not-found").header("Accept-Language", "en"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.code").value("artwork_not_found"))
                .andExpect(jsonPath("$.message").value("Artwork 1234 not found"))
                .andExpect(jsonPath("$.path").value("/not-found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void businessRuleIs409ByDefault() throws Exception {
        mockMvc.perform(get("/conflict").header("Accept-Language", "en"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("master_already_has_store"))
                .andExpect(jsonPath("$.message").value("Master 7 already has a store"));
    }

    @Test
    void businessRuleCanBe422WithArguments() throws Exception {
        mockMvc.perform(get("/too-low").header("Accept-Language", "en"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("bid_too_low"))
                .andExpect(jsonPath("$.message").value("The bid must be at least 2,500,000.5 COP"));
    }

    @Test
    void messagesFollowTheRequestLanguage() throws Exception {
        mockMvc.perform(get("/not-found").header("Accept-Language", "es-CO"))
                .andExpect(jsonPath("$.code").value("artwork_not_found"))
                .andExpect(jsonPath("$.message").value("No se encontró la obra 1234"));
        mockMvc.perform(get("/too-low"))
                .andExpect(jsonPath("$.message").value("La puja debe ser de al menos 2.500.000,5 COP"));
        mockMvc.perform(get("/too-low").header("Accept-Language", "en"))
                .andExpect(jsonPath("$.message").value("The bid must be at least 2,500,000.5 COP"));
    }

    @Test
    void businessRuleCanBe429() throws Exception {
        mockMvc.perform(get("/too-many"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("bid_rate_limited"));
    }

    @Test
    void featureDisabledIs503() throws Exception {
        mockMvc.perform(get("/disabled"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("ai_disabled"));
    }

    @Test
    void dataIntegrityIs409() throws Exception {
        mockMvc.perform(get("/integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("data_conflict"));
    }

    @Test
    void invalidBodyIs400WithFieldList() throws Exception {
        mockMvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content("{\"fileUrl\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("fileUrl"));
    }

    @Test
    void malformedJsonIs400() throws Exception {
        mockMvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content("{oops"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("malformed_request"));
    }
}
