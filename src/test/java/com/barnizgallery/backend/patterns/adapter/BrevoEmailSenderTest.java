package com.barnizgallery.backend.patterns.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.barnizgallery.backend.config.BrevoProperties;
import com.barnizgallery.backend.config.MailConfig;

/** Brevo is simulated with MockRestServiceServer; no real email is sent. */
class BrevoEmailSenderTest {

    private static final String BASE_URL = "https://brevo.test";

    private final String apiKey = UUID.randomUUID().toString();
    private final BrevoProperties properties = new BrevoProperties(apiKey, "no-reply@museo.test", "MUSEO",
            BASE_URL, Duration.ofSeconds(10));
    private final EmailMessage message = new EmailMessage("ana@mail.com", "Ana", "Asunto", "<p>Hola</p>", "Hola");
    private MockRestServiceServer server;
    private BrevoEmailSender sender;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL)
                .defaultHeader(BrevoEmailSender.API_KEY_HEADER, apiKey);
        server = MockRestServiceServer.bindTo(builder).build();
        sender = new BrevoEmailSender(builder.build(), properties);
    }

    @Test
    void mapsTheMessageToTheBrevoRequest() {
        server.expect(requestTo(BASE_URL + "/v3/smtp/email"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("api-key", apiKey))
                .andExpect(jsonPath("$.sender.email").value("no-reply@museo.test"))
                .andExpect(jsonPath("$.sender.name").value("MUSEO"))
                .andExpect(jsonPath("$.to[0].email").value("ana@mail.com"))
                .andExpect(jsonPath("$.to[0].name").value("Ana"))
                .andExpect(jsonPath("$.subject").value("Asunto"))
                .andExpect(jsonPath("$.htmlContent").value("<p>Hola</p>"))
                .andExpect(jsonPath("$.textContent").value("Hola"))
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"messageId\": \"<abc@smtp-relay>\"}"));

        sender.send(message);

        server.verify();
    }

    @Test
    void brevoFailuresNeverBreakTheFlow() {
        server.expect(requestTo(BASE_URL + "/v3/smtp/email"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\": \"unauthorized\", \"message\": \"Key not found\"}"));
        server.expect(requestTo(BASE_URL + "/v3/smtp/email"))
                .andRespond(withException(new SocketTimeoutException("Read timed out")));

        assertThatCode(() -> sender.send(message)).doesNotThrowAnyException();
        assertThatCode(() -> sender.send(message)).doesNotThrowAnyException();
    }

    @Test
    void addressesAreMaskedInLogs() {
        assertThat(BrevoEmailSender.masked("ana@mail.com")).isEqualTo("a***@mail.com");
        assertThat(BrevoEmailSender.masked("broken")).isEqualTo("***");
    }

    @Test
    void missingKeyOrSenderDisablesEmails() {
        MailConfig config = new MailConfig();

        assertThat(config.emailSender(new BrevoProperties("", "no-reply@museo.test", "MUSEO", BASE_URL,
                Duration.ofSeconds(10)))).isInstanceOf(DisabledEmailSender.class);
        assertThat(config.emailSender(new BrevoProperties(apiKey, "", "MUSEO", BASE_URL, Duration.ofSeconds(10)))
                .isEnabled()).isFalse();
        assertThat(config.emailSender(properties)).isInstanceOf(BrevoEmailSender.class);
    }
}
