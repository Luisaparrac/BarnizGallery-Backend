package com.barnizgallery.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.response.HealthResponse;
import com.barnizgallery.backend.patterns.adapter.StorageService;
import com.barnizgallery.backend.patterns.facade.AiFacade;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Simple health endpoint used by Render and by the frontend to know
 * which optional integrations are enabled.
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "System")
public class HealthController {

    private final AiFacade aiFacade;
    private final StorageService storageService;

    public HealthController(AiFacade aiFacade, StorageService storageService) {
        this.aiFacade = aiFacade;
        this.storageService = storageService;
    }

    @GetMapping
    public HealthResponse health() {
        // Hyper3D is wired in the last phase; until then it is reported as disabled.
        return new HealthResponse("UP", aiFacade.isAiEnabled(), false, storageService.isEnabled());
    }
}
