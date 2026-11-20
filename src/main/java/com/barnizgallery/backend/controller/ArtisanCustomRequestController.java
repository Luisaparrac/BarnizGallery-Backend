package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.CustomRequestResponse;
import com.barnizgallery.backend.service.ArtisanCustomRequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Custom requests for approved artisans.
 */
@RestController
@RequestMapping("/api/artisan/custom-requests")
@Tag(name = "Custom orders")
public class ArtisanCustomRequestController {

    private final ArtisanCustomRequestService service;

    public ArtisanCustomRequestController(ArtisanCustomRequestService service) {
        this.service = service;
    }

    @GetMapping("/open")
    @Operation(summary = "Open requests for everybody or addressed to the artisan")
    public List<CustomRequestResponse> open() {
        return service.open();
    }

    @GetMapping("/accepted")
    @Operation(summary = "Requests where the artisan's offer was accepted")
    public List<CustomRequestResponse> accepted() {
        return service.accepted();
    }

    @GetMapping("/declined")
    @Operation(summary = "Declined requests (always empty: declines are not stored yet)")
    public List<CustomRequestResponse> declined() {
        return service.declined();
    }
}
