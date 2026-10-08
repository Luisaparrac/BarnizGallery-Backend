package com.barnizgallery.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.barnizgallery.backend.dto.IdentifyVisitorRequest;
import com.barnizgallery.backend.dto.IdentifyVisitorResponse;
import com.barnizgallery.backend.dto.VisitorResponse;
import com.barnizgallery.backend.enums.CameraMode;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.exception.GlobalExceptionHandler;
import com.barnizgallery.backend.service.VisitorService;

/**
 * Web test with a mocked service (no Spring context, no database).
 */
class VisitorControllerTest {

    private final VisitorService visitorService = mock(VisitorService.class);
    private MockMvc mockMvc;

    private static final String BODY = """
            {"email":"ana@mail.com","name":"Ana","country":"Colombia",
             "preferredLanguage":"ES","cameraMode":"FIRST_PERSON"}
            """;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new VisitorController(visitorService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void identifyNewVisitorReturns201AndIsNewTrue() throws Exception {
        when(visitorService.identify(any(IdentifyVisitorRequest.class))).thenReturn(response(true));

        mockMvc.perform(post("/api/visitors/identify").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isNew").value(true))
                .andExpect(jsonPath("$.visitor.email").value("ana@mail.com"));
    }

    @Test
    void identifyExistingVisitorReturns200() throws Exception {
        when(visitorService.identify(any(IdentifyVisitorRequest.class))).thenReturn(response(false));

        mockMvc.perform(post("/api/visitors/identify").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isNew").value(false));
    }

    @Test
    void invalidEmailIs400() throws Exception {
        mockMvc.perform(post("/api/visitors/identify").contentType(MediaType.APPLICATION_JSON)
                .content(BODY.replace("ana@mail.com", "not-an-email")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("email"));
    }

    private static IdentifyVisitorResponse response(boolean isNew) {
        return new IdentifyVisitorResponse(
                new VisitorResponse(1, "Ana", "ana@mail.com", "Colombia", Language.ES, CameraMode.FIRST_PERSON),
                isNew);
    }
}
