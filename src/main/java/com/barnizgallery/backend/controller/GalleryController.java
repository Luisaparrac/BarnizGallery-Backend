package com.barnizgallery.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.GalleryNodeResponse;
import com.barnizgallery.backend.service.GalleryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Full gallery tree used by the frontend to build the 3D world.
 */
@RestController
@RequestMapping("/api/gallery")
@Tag(name = "Gallery")
public class GalleryController {

    private final GalleryService galleryService;

    public GalleryController(GalleryService galleryService) {
        this.galleryService = galleryService;
    }

    @GetMapping
    @Operation(summary = "Gallery tree: gallery → rooms → artworks (Composite pattern)")
    public GalleryNodeResponse getGallery() {
        return galleryService.getGallery();
    }
}
