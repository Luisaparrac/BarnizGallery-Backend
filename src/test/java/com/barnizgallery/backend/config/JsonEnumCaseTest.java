package com.barnizgallery.backend.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import com.barnizgallery.backend.dto.RegisterRequest;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.enums.UserRole;

import tools.jackson.databind.json.JsonMapper;

/** The JSON mapper of the application reads enum values in any case, as the frontend sends them. */
@JsonTest
class JsonEnumCaseTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void theFrontendSignUpBodyIsReadWithLowerCaseEnums() {
        RegisterRequest lower = jsonMapper.readValue("""
                {"fullName": "Prueba", "email": "prueba@example.com", "password": "Prueba12345",
                 "preferredLanguage": "es", "role": "customer"}""", RegisterRequest.class);
        RegisterRequest upper = jsonMapper.readValue("""
                {"fullName": "Prueba", "email": "prueba@example.com", "password": "Prueba12345",
                 "preferredLanguage": "EN", "role": "CUSTOMER"}""", RegisterRequest.class);

        assertThat(lower.preferredLanguage()).isEqualTo(Language.ES);
        assertThat(lower.role()).isEqualTo(UserRole.CUSTOMER);
        assertThat(upper.preferredLanguage()).isEqualTo(Language.EN);
        assertThat(upper.role()).isEqualTo(UserRole.CUSTOMER);
    }
}
