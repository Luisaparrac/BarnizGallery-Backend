package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.response.RecommendationResponse;
import com.barnizgallery.backend.service.RecommendationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Room recommendations for a visitor (Strategy pattern through the AI Facade).
 */
@RestController
@RequestMapping("/api/visitors/{id}/recommendations")
@Tag(name = "Recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    @Operation(summary = "Compute recommendations (strategy = profile | interactions | hybrid | ai) "
            + "and replace the previous ones")
    public List<RecommendationResponse> compute(@PathVariable Integer id,
            @RequestParam(defaultValue = "hybrid") String strategy) {
        return recommendationService.compute(id, strategy);
    }

    @GetMapping
    public List<RecommendationResponse> latest(@PathVariable Integer id) {
        return recommendationService.latest(id);
    }
}
