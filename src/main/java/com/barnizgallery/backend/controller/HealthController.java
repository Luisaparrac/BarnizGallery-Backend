package com.barnizgallery.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.response.HealthResponse;

/**
 * Simple health endpoint used by Render and by the frontend to know
 * which optional integrations are enabled.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public HealthResponse health() {
        // Integrations are wired in later phases; until then they are disabled.
        return new HealthResponse("UP", false, false, false);
    }
}
