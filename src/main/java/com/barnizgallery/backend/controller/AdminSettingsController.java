package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.AppSettingRequest;
import com.barnizgallery.backend.dto.AppSettingResponse;
import com.barnizgallery.backend.service.AppSettingsAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Business settings ({@code app_settings}). Only for administrators.
 */
@RestController
@RequestMapping("/api/admin/settings")
@Tag(name = "Administration")
public class AdminSettingsController {

    private final AppSettingsAdminService settingsService;

    public AdminSettingsController(AppSettingsAdminService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    @Operation(summary = "Every setting with its JSON value (null = not configured)")
    public List<AppSettingResponse> findAll() {
        return settingsService.findAll();
    }

    @PutMapping("/{key}")
    @Operation(summary = "Change the JSON value of an existing setting; audited with the administrator's id")
    public AppSettingResponse update(@PathVariable String key, @Valid @RequestBody AppSettingRequest request) {
        return settingsService.update(key, request.value());
    }
}
