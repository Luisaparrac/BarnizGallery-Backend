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

import com.barnizgallery.backend.config.MessageConfig;
import com.barnizgallery.backend.dto.IdentifyUserRequest;
import com.barnizgallery.backend.dto.IdentifyUserResponse;
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

    private static final String BODY = """
            {"email":"ana@mail.com","name":"Ana","countryCode":"CO",
             "preferredLanguage":"ES"}
            """;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
                .setControllerAdvice(new GlobalExceptionHandler(new MessageConfig().messageSource()))
                .build();
    }

    @Test
    void identifyNewUserReturns201AndIsNewTrue() throws Exception {
        when(userService.identify(any(IdentifyUserRequest.class))).thenReturn(response(true));

        mockMvc.perform(post("/api/users/identify").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isNew").value(true))
                .andExpect(jsonPath("$.user.email").value("ana@mail.com"));
    }

    @Test
    void identifyExistingUserReturns200() throws Exception {
        when(userService.identify(any(IdentifyUserRequest.class))).thenReturn(response(false));

        mockMvc.perform(post("/api/users/identify").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isNew").value(false));
    }

    @Test
    void invalidEmailIs400() throws Exception {
        mockMvc.perform(post("/api/users/identify").contentType(MediaType.APPLICATION_JSON)
                .content(BODY.replace("ana@mail.com", "not-an-email")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("email"));
    }

    private static IdentifyUserResponse response(boolean isNew) {
        return new IdentifyUserResponse(
                new UserResponse(1L, "Ana", "ana@mail.com", UserRole.CUSTOMER, UserStatus.PENDING, "CO",
                        Language.ES),
                isNew);
    }
}
