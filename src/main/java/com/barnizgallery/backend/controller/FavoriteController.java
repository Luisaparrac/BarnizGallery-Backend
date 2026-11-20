package com.barnizgallery.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.service.FavoriteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Favorite artworks of the logged-in user.
 */
@RestController
@RequestMapping("/api/users/me/favorites")
@Tag(name = "Users")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PutMapping("/{artworkId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Save an artwork of the public gallery as favorite (idempotent)")
    public void add(@PathVariable Long artworkId) {
        favoriteService.add(artworkId);
    }

    @DeleteMapping("/{artworkId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove an artwork from the favorites (idempotent)")
    public void remove(@PathVariable Long artworkId) {
        favoriteService.remove(artworkId);
    }
}
