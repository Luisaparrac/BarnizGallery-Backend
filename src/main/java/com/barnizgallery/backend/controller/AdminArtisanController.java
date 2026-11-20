package com.barnizgallery.backend.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.ArtisanVerificationResponse;
import com.barnizgallery.backend.service.VerificationReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Final decisions on master artisans. Only for administrators.
 */
@RestController
@RequestMapping("/api/admin/artisans")
@Tag(name = "Administration")
public class AdminArtisanController {

    private final VerificationReviewService reviewService;

    public AdminArtisanController(VerificationReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/{masterId}/approve")
    @Operation(summary = "Approve a master whose email, identity, workshop and samples steps are approved (409 if not)")
    public ArtisanVerificationResponse approve(@PathVariable Long masterId) {
        return reviewService.approveArtisan(masterId);
    }
}
