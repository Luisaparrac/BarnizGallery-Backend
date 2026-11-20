package com.barnizgallery.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.TasteProfileRequest;
import com.barnizgallery.backend.dto.TasteProfileResponse;
import com.barnizgallery.backend.dto.UserPreferencesRequest;
import com.barnizgallery.backend.dto.UserResponse;
import com.barnizgallery.backend.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * User preferences and taste profile. Accounts are created through {@code /api/auth/register}.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PatchMapping("/{id}/preferences")
    public UserResponse updatePreferences(@PathVariable Long id,
            @Valid @RequestBody UserPreferencesRequest request) {
        return userService.updatePreferences(id, request);
    }

    @GetMapping("/{id}/taste-profile")
    public TasteProfileResponse getTasteProfile(@PathVariable Long id) {
        return userService.getTasteProfile(id);
    }

    @PutMapping("/{id}/taste-profile")
    public TasteProfileResponse saveTasteProfile(@PathVariable Long id,
            @Valid @RequestBody TasteProfileRequest request) {
        return userService.saveTasteProfile(id, request);
    }
}
