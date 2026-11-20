package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.RecommendationPreviewResponse;
import com.barnizgallery.backend.dto.TastePreviewRequest;
import com.barnizgallery.backend.service.RecommendationPreviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Taste kiosk of the visitor: public, nothing is saved.
 */
@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "Recommendations")
public class RecommendationPreviewController {

    private final RecommendationPreviewService previewService;

    public RecommendationPreviewController(RecommendationPreviewService previewService) {
        this.previewService = previewService;
    }

    @PostMapping("/preview")
    @Operation(summary = "Public: artworks of the catalog that match the kiosk answers (catalog ids); not saved")
    public List<RecommendationPreviewResponse> preview(@RequestBody TastePreviewRequest request) {
        return previewService.preview(request);
    }
}
