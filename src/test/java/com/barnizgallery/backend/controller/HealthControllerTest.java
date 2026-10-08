package com.barnizgallery.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.dto.response.HealthResponse;
import com.barnizgallery.backend.patterns.adapter.DisabledStorageService;
import com.barnizgallery.backend.patterns.facade.AiFacade;

class HealthControllerTest {

    @Test
    void healthReportsUpAndIntegrationFlags() {
        AiFacade aiFacade = mock(AiFacade.class);
        when(aiFacade.isAiEnabled()).thenReturn(false);

        HealthResponse body = new HealthController(aiFacade, new DisabledStorageService()).health();

        assertThat(body.status()).isEqualTo("UP");
        assertThat(body.aiEnabled()).isFalse();
        assertThat(body.hyper3dEnabled()).isFalse();
        assertThat(body.storageEnabled()).isFalse();
    }
}
