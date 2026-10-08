package com.barnizgallery.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class HealthControllerTest {

    @Test
    void healthReportsUpAndDisabledIntegrations() {
        Map<String, Object> body = new HealthController().health();

        assertThat(body).containsEntry("status", "UP")
                .containsEntry("aiEnabled", false)
                .containsEntry("hyper3dEnabled", false)
                .containsEntry("storageEnabled", false);
    }
}
