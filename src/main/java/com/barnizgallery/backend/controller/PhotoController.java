package com.barnizgallery.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.service.PhotoService;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Operations on a single photo.
 */
@RestController
@RequestMapping("/api/photos")
@Tag(name = "Artworks")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        photoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
