package com.barnizgallery.backend.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.request.PhotoRequest;

import jakarta.validation.Valid;

/**
 * Checks the JSON error format with a small fake controller (no Spring context, no database).
 */
class GlobalExceptionHandlerTest {

    @RestController
    @Validated
    static class FakeController {

        @GetMapping("/not-found")
        void notFound() {
            throw new ResourceNotFoundException("Artwork", 99);
        }

        @GetMapping("/conflict")
        void conflict() {
            throw new BusinessRuleException("Room already exists");
        }

        @GetMapping("/too-many")
        void tooMany() {
            throw BusinessRuleException.tooManyRequests("Slow down");
        }

        @GetMapping("/disabled")
        void disabled() {
            throw new FeatureDisabledException("AI is disabled");
        }

        @GetMapping("/integrity")
        void integrity() {
            throw new DataIntegrityViolationException("duplicate key");
        }

        @PostMapping("/validate")
        void validate(@Valid @RequestBody PhotoRequest request) {
        }
    }

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FakeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void notFoundIs404WithMessageAndPath() throws Exception {
        mockMvc.perform(get("/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Artwork 99 not found"))
                .andExpect(jsonPath("$.path").value("/not-found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void businessRuleIs409ByDefault() throws Exception {
        mockMvc.perform(get("/conflict")).andExpect(status().isConflict());
    }

    @Test
    void businessRuleCanBe429() throws Exception {
        mockMvc.perform(get("/too-many")).andExpect(status().isTooManyRequests());
    }

    @Test
    void featureDisabledIs503() throws Exception {
        mockMvc.perform(get("/disabled"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("AI is disabled"));
    }

    @Test
    void dataIntegrityIs409() throws Exception {
        mockMvc.perform(get("/integrity")).andExpect(status().isConflict());
    }

    @Test
    void invalidBodyIs400WithFieldList() throws Exception {
        mockMvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content("{\"fileUrl\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("fileUrl"));
    }

    @Test
    void malformedJsonIs400() throws Exception {
        mockMvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content("{oops"))
                .andExpect(status().isBadRequest());
    }
}
