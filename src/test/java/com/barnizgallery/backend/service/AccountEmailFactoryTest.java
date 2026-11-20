package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.config.MessageConfig;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.patterns.adapter.EmailMessage;

class AccountEmailFactoryTest {

    private final AccountEmailFactory factory = new AccountEmailFactory(new MessageConfig().messageSource(),
            "https://museo.test/");

    @Test
    void verificationEmailIsInSpanishForSpanishUsers() {
        EmailMessage email = factory.verification(TestEntities.user(1, Language.ES), "tok/en+1", Duration.ofHours(24));

        assertThat(email.toEmail()).isEqualTo("user1@test.local");
        assertThat(email.subject()).isEqualTo("Confirma tu correo · MUSEO Barniz de Pasto");
        assertThat(email.text()).contains("Hola, User 1:")
                .contains("https://museo.test/verify-email?token=tok%2Fen%2B1")
                .contains("El enlace vence en 24 horas");
        assertThat(email.html()).contains("Confirmar mi correo")
                .contains("href=\"https://museo.test/verify-email?token=tok%2Fen%2B1\"");
    }

    @Test
    void passwordResetEmailIsInEnglishForEnglishUsers() {
        EmailMessage email = factory.passwordReset(TestEntities.user(2, Language.EN), "abc", Duration.ofHours(1));

        assertThat(email.subject()).isEqualTo("Choose a new password · MUSEO Barniz de Pasto");
        assertThat(email.text()).contains("Hello, User 2:")
                .contains("https://museo.test/reset-password?token=abc")
                .contains("The link expires in 1 hour and");
    }

    @Test
    void userValuesAreEscapedInTheHtml() {
        User user = TestEntities.user(3, Language.EN);
        user.setName("<script>alert(1)</script>");

        EmailMessage email = factory.verification(user, "abc", Duration.ofHours(24));

        assertThat(email.html()).doesNotContain("<script>").contains("&lt;script&gt;");
    }
}
