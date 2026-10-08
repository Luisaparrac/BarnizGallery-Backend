package com.barnizgallery.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.request.IdentifyVisitorRequest;
import com.barnizgallery.backend.dto.request.TasteProfileRequest;
import com.barnizgallery.backend.dto.request.VisitorPreferencesRequest;
import com.barnizgallery.backend.dto.response.IdentifyVisitorResponse;
import com.barnizgallery.backend.dto.response.TasteProfileResponse;
import com.barnizgallery.backend.dto.response.VisitorResponse;
import com.barnizgallery.backend.service.VisitorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Visitor identification (email only), preferences and taste profile.
 */
@RestController
@RequestMapping("/api/visitors")
@Tag(name = "Visitors")
public class VisitorController {

    private final VisitorService visitorService;

    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    @PostMapping("/identify")
    @Operation(summary = "Find a visitor by email, or create it (201) if it does not exist")
    public ResponseEntity<IdentifyVisitorResponse> identify(@Valid @RequestBody IdentifyVisitorRequest request) {
        IdentifyVisitorResponse response = visitorService.identify(request);
        return ResponseEntity.status(response.isNew() ? HttpStatus.CREATED : HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public VisitorResponse findById(@PathVariable Integer id) {
        return visitorService.findById(id);
    }

    @PatchMapping("/{id}/preferences")
    public VisitorResponse updatePreferences(@PathVariable Integer id,
            @Valid @RequestBody VisitorPreferencesRequest request) {
        return visitorService.updatePreferences(id, request);
    }

    @GetMapping("/{id}/taste-profile")
    public TasteProfileResponse getTasteProfile(@PathVariable Integer id) {
        return visitorService.getTasteProfile(id);
    }

    @PutMapping("/{id}/taste-profile")
    public TasteProfileResponse saveTasteProfile(@PathVariable Integer id,
            @Valid @RequestBody TasteProfileRequest request) {
        return visitorService.saveTasteProfile(id, request);
    }
}
