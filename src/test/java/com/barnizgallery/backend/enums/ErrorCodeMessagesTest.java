package com.barnizgallery.backend.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Every error code must have its text in every message bundle, so no user ever sees a bare code.
 */
class ErrorCodeMessagesTest {

    @ParameterizedTest
    @ValueSource(strings = { "messages_en.properties", "messages_es.properties" })
    void everyErrorCodeHasAText(String bundle) throws IOException {
        Properties texts = load(bundle);

        assertThat(Arrays.stream(ErrorCode.values()).map(ErrorCode::messageKey))
                .allSatisfy(key -> assertThat(texts.getProperty(key)).as(key).isNotBlank());
    }

    private static Properties load(String bundle) throws IOException {
        Properties properties = new Properties();
        try (InputStream in = ErrorCodeMessagesTest.class.getClassLoader().getResourceAsStream(bundle)) {
            assertThat(in).as(bundle).isNotNull();
            properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        return properties;
    }
}
