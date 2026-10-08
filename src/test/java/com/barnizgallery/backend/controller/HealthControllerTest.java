package com.barnizgallery.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.dto.HealthResponse;
import com.barnizgallery.backend.patterns.adapter.DisabledStorageService;
import com.barnizgallery.backend.patterns.adapter.ThreeDModelGenerator;
import com.barnizgallery.backend.patterns.facade.AiFacade;

class HealthControllerTest {

    @Test
    void healthReportsUpAndIntegrationFlags() {
        AiFacade aiFacade = mock(AiFacade.class);
        ThreeDModelGenerator generator = mock(ThreeDModelGenerator.class);
        when(aiFacade.isAiEnabled()).thenReturn(false);
        when(generator.isEnabled()).thenReturn(true);

        HealthResponse body = new HealthController(aiFacade, generator, new DisabledStorageService()).health();

        assertThat(body.status()).isEqualTo("UP");
        assertThat(body.aiEnabled()).isFalse();
        assertThat(body.hyper3dEnabled()).isTrue();
        assertThat(body.storageEnabled()).isFalse();
    }
}
