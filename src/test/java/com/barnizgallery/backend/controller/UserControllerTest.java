package com.barnizgallery.backend.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.unit.DataSize;

import com.barnizgallery.backend.config.MessageConfig;
import com.barnizgallery.backend.dto.UserResponse;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.UserRole;
import com.barnizgallery.backend.enums.UserStatus;
import com.barnizgallery.backend.exception.GlobalExceptionHandler;
import com.barnizgallery.backend.service.UserService;

/**
 * Web test with a mocked service (no Spring context, no database).
 */
class UserControllerTest {

    private final UserService userService = mock(UserService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
                .setControllerAdvice(new GlobalExceptionHandler(new MessageConfig().messageSource(),
                        DataSize.ofMegabytes(5)))
                .build();
    }

    @Test
    void findByIdReturnsTheUser() throws Exception {
        when(userService.findById(1L)).thenReturn(new UserResponse(1L, "Ana", "ana@mail.com", UserRole.CUSTOMER,
                UserStatus.ACTIVE, "CO", Language.ES, null));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@mail.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void unknownLanguageIs400() throws Exception {
        mockMvc.perform(patch("/api/users/1/preferences").contentType(MediaType.APPLICATION_JSON)
                .content("{\"preferredLanguage\":\"FR\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("malformed_request"));
    }
}
