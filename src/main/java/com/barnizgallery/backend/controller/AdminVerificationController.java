package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.ArtisanVerificationResponse;
import com.barnizgallery.backend.service.VerificationReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Review of the master artisans' verification. Only for administrators.
 */
@RestController
@RequestMapping("/api/admin/verifications")
@Tag(name = "Administration")
public class AdminVerificationController {

    private final VerificationReviewService reviewService;

    public AdminVerificationController(VerificationReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    @Operation(summary = "Masters waiting for a verification decision, with their open steps")
    public List<ArtisanVerificationResponse> pending() {
        return reviewService.pending();
    }
}
