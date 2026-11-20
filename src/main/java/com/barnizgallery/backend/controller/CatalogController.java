package com.barnizgallery.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.CatalogsResponse;
import com.barnizgallery.backend.service.CatalogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Public catalogs used by the forms of the frontend. Requests always refer to catalog entries by code.
 */
@RestController
@RequestMapping("/api/catalogs")
@Tag(name = "Catalogs")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    @Operation(summary = "Active colors, shapes, motifs, styles, budget ranges, avatars and store templates")
    public CatalogsResponse catalogs() {
        return catalogService.activeCatalogs();
    }
}
