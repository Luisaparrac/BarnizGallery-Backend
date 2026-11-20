package com.barnizgallery.backend.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.service.PublicSettingsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import tools.jackson.databind.JsonNode;

/**
 * Settings the frontend reads without logging in.
 */
@RestController
@RequestMapping("/api/settings")
@Tag(name = "Catalogs")
public class SettingsController {

    private final PublicSettingsService publicSettingsService;

    public SettingsController(PublicSettingsService publicSettingsService) {
        this.publicSettingsService = publicSettingsService;
    }

    @GetMapping("/public")
    @Operation(summary = "Public settings: default currency, languages, minimum bid increment, cart reservation "
            + "minutes and shipping amounts (null when not configured)")
    public Map<String, JsonNode> publicSettings() {
        return publicSettingsService.values();
    }
}
