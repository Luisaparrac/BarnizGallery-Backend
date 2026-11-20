package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.ArtworkRequest;
import com.barnizgallery.backend.dto.ArtworkStatusRequest;
import com.barnizgallery.backend.dto.ArtworkSummaryResponse;
import com.barnizgallery.backend.service.ArtisanArtworkService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Artworks of the logged-in artisan's store. Only for artisans.
 */
@RestController
@RequestMapping("/api/artisans/me/artworks")
@Tag(name = "Artisans")
public class ArtisanArtworkController {

    private final ArtisanArtworkService artworkService;

    public ArtisanArtworkController(ArtisanArtworkService artworkService) {
        this.artworkService = artworkService;
    }

    @GetMapping
    @Operation(summary = "Every artwork of the artisan's store, drafts included")
    public List<ArtworkSummaryResponse> mine() {
        return artworkService.mine();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a draft artwork in the artisan's store")
    public ArtworkSummaryResponse create(@Valid @RequestBody ArtworkRequest request) {
        return artworkService.create(request);
    }

    @PatchMapping("/{artworkId}/status")
    @Operation(summary = "Move an artwork between DRAFT, EXHIBITED and ARCHIVED (409 for any other change)")
    public ArtworkSummaryResponse changeStatus(@PathVariable Long artworkId,
            @Valid @RequestBody ArtworkStatusRequest request) {
        return artworkService.changeStatus(artworkId, request.status());
    }

    @DeleteMapping("/{artworkId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Soft delete an artwork (409 while it is in auction or reserved)")
    public void delete(@PathVariable Long artworkId) {
        artworkService.delete(artworkId);
    }

    @PutMapping("/{artworkId}")
    @Operation(summary = "Replace the data of an artwork of the artisan's store; the status does not change")
    public ArtworkSummaryResponse update(@PathVariable Long artworkId, @Valid @RequestBody ArtworkRequest request) {
        return artworkService.update(artworkId, request);
    }
}
