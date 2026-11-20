package com.barnizgallery.backend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.TasteProfileRequest;
import com.barnizgallery.backend.dto.TasteProfileResponse;
import com.barnizgallery.backend.dto.UpdateProfileRequest;
import com.barnizgallery.backend.dto.UserPreferencesRequest;
import com.barnizgallery.backend.dto.UserResponse;
import com.barnizgallery.backend.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
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

    @GetMapping("/me")
    @Operation(summary = "Profile of the logged-in user")
    public UserResponse me() {
        return userService.me();
    }

    @PatchMapping("/me")
    @Operation(summary = "Change name, phone, country, language or avatar (by catalog code) of the logged-in user")
    public UserResponse updateMe(@Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateMe(request);
    }

    @GetMapping("/me/taste-profile")
    @Operation(summary = "Taste profile of the logged-in user")
    public TasteProfileResponse myTasteProfile() {
        return userService.myTasteProfile();
    }

    @PutMapping("/me/taste-profile")
    @Operation(summary = "Save the questionnaire of the logged-in user with catalog codes (unknown or inactive → 422)")
    public TasteProfileResponse saveMyTasteProfile(@Valid @RequestBody TasteProfileRequest request) {
        return userService.saveMyTasteProfile(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@access.isSelfOrAdmin(#id)")
    public UserResponse findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PatchMapping("/{id}/preferences")
    @PreAuthorize("@access.isSelf(#id)")
    public UserResponse updatePreferences(@PathVariable Long id,
            @Valid @RequestBody UserPreferencesRequest request) {
        return userService.updatePreferences(id, request);
    }

    @GetMapping("/{id}/taste-profile")
    @PreAuthorize("@access.isSelfOrAdmin(#id)")
    public TasteProfileResponse getTasteProfile(@PathVariable Long id) {
        return userService.getTasteProfile(id);
    }

    @PutMapping("/{id}/taste-profile")
    @PreAuthorize("@access.isSelf(#id)")
    public TasteProfileResponse saveTasteProfile(@PathVariable Long id,
            @Valid @RequestBody TasteProfileRequest request) {
        return userService.saveTasteProfile(id, request);
    }
}
