package com.barnizgallery.backend.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple health endpoint used by Render and by the frontend to know
 * which optional integrations are enabled.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        // Integrations are wired in later phases; until then they are disabled.
        body.put("aiEnabled", false);
        body.put("hyper3dEnabled", false);
        body.put("storageEnabled", false);
        return body;
    }
}
