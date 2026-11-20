package com.barnizgallery.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.PhotoResponse;
import com.barnizgallery.backend.dto.PhotoUpdateRequest;
import com.barnizgallery.backend.service.PhotoArrangementService;
import com.barnizgallery.backend.service.PhotoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Operations on a single photo.
 */
@RestController
@RequestMapping("/api/photos")
@Tag(name = "Artworks")
public class PhotoController {

    private final PhotoService photoService;
    private final PhotoArrangementService arrangementService;

    public PhotoController(PhotoService photoService, PhotoArrangementService arrangementService) {
        this.photoService = photoService;
        this.arrangementService = arrangementService;
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Change alternative texts or angle, or make it the primary photo (owner artisan)")
    public PhotoResponse update(@PathVariable Long id, @Valid @RequestBody PhotoUpdateRequest request) {
        return arrangementService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        photoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
