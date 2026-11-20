package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.AdminArtworkUpdateRequest;
import com.barnizgallery.backend.dto.ArtworkSummaryResponse;
import com.barnizgallery.backend.service.AdminArtworkService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Sale data of any artwork. Only for administrators.
 */
@RestController
@RequestMapping("/api/admin/artworks")
@Tag(name = "Administration")
public class AdminArtworkController {

    private final AdminArtworkService artworkService;

    public AdminArtworkController(AdminArtworkService artworkService) {
        this.artworkService = artworkService;
    }

    @GetMapping
    @Operation(summary = "Every artwork of a store (drafts included)")
    public List<ArtworkSummaryResponse> ofStore(@RequestParam Long storeId) {
        return artworkService.ofStore(storeId);
    }

    @PatchMapping("/{artworkId}")
    @Operation(summary = "Set price, currency, stock, sale mode or status (DRAFT, EXHIBITED, ARCHIVED, SOLD)")
    public ArtworkSummaryResponse update(@PathVariable Long artworkId,
            @Valid @RequestBody AdminArtworkUpdateRequest request) {
        return artworkService.update(artworkId, request);
    }
}
