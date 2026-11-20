package com.barnizgallery.backend.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.barnizgallery.backend.dto.EvidenceRequest;
import com.barnizgallery.backend.dto.VerificationStepResponse;
import com.barnizgallery.backend.enums.VerificationStep;
import com.barnizgallery.backend.service.ArtisanVerificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * The logged-in master artisan's own verification. Only for artisans.
 */
@RestController
@RequestMapping("/api/artisans/me/verification")
@Tag(name = "Artisans")
public class ArtisanController {

    private final ArtisanVerificationService verificationService;

    public ArtisanController(ArtisanVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PutMapping("/{step}")
    @Operation(summary = "Send the evidence link of IDENTITY, WORKSHOP or SAMPLES; the step becomes SUBMITTED")
    public VerificationStepResponse submitEvidence(@PathVariable VerificationStep step,
            @Valid @RequestBody EvidenceRequest request) {
        return verificationService.submitEvidence(step, request.evidenceUrl());
    }

    @PostMapping(value = "/{step}/evidence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload an evidence image (JPEG/PNG/WebP) of a step; 503 while photo storage is disabled")
    public VerificationStepResponse uploadEvidence(@PathVariable VerificationStep step,
            @RequestPart("file") MultipartFile file) {
        return verificationService.uploadEvidence(step, file);
    }
}
