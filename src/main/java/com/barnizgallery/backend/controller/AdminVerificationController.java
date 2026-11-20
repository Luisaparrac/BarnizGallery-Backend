package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.ArtisanVerificationResponse;
import com.barnizgallery.backend.dto.StepReviewRequest;
import com.barnizgallery.backend.dto.VerificationStepResponse;
import com.barnizgallery.backend.service.VerificationReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

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

    @PutMapping("/{verificationId}")
    @Operation(summary = "Approve or reject an IDENTITY, WORKSHOP or SAMPLES step with notes for the artisan")
    public VerificationStepResponse review(@PathVariable Long verificationId,
            @Valid @RequestBody StepReviewRequest request) {
        return reviewService.review(verificationId, request);
    }
}
