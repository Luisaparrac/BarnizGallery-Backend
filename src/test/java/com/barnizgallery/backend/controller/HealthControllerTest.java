package com.barnizgallery.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.dto.response.HealthResponse;

class HealthControllerTest {

    @Test
    void healthReportsUpAndDisabledIntegrations() {
        HealthResponse body = new HealthController().health();

        assertThat(body.status()).isEqualTo("UP");
        assertThat(body.aiEnabled()).isFalse();
        assertThat(body.hyper3dEnabled()).isFalse();
        assertThat(body.storageEnabled()).isFalse();
    }
}
