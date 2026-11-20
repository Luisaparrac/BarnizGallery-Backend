package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.RecommendationResponse;
import com.barnizgallery.backend.service.RecommendationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Store recommendations for a user (Strategy pattern through the AI Facade).
 */
@RestController
@RequestMapping("/api/users/{id}/recommendations")
@Tag(name = "Recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    @PreAuthorize("@access.isSelfOrAdmin(#id)")
    @Operation(summary = "Compute recommendations of stores or artworks (target = stores | artworks; strategy = "
            + "profile | interactions | hybrid | ai | ai-service) and replace the previous ones of that target")
    public List<RecommendationResponse> compute(@PathVariable Long id,
            @RequestParam(defaultValue = "hybrid") String strategy,
            @RequestParam(defaultValue = RecommendationService.STORES) String target) {
        return recommendationService.compute(id, strategy, target);
    }

    @GetMapping
    @PreAuthorize("@access.isSelfOrAdmin(#id)")
    public List<RecommendationResponse> latest(@PathVariable Long id) {
        return recommendationService.latest(id);
    }
}
