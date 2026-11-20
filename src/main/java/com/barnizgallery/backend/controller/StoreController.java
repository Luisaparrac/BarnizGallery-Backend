package com.barnizgallery.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.StorePageResponse;
import com.barnizgallery.backend.service.StoreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Public pages of the published stores.
 */
@RestController
@RequestMapping("/api/stores")
@Tag(name = "Stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Public page of a published store: visible sections and exhibited or auctioned artworks")
    public StorePageResponse findBySlug(@PathVariable String slug) {
        return storeService.findPublishedBySlug(slug);
    }
}
