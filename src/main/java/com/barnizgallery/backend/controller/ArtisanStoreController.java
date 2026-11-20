package com.barnizgallery.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.CreateStoreRequest;
import com.barnizgallery.backend.dto.StoreEditorResponse;
import com.barnizgallery.backend.service.StoreEditorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Store editor of the logged-in artisan. Only for artisans.
 */
@RestController
@RequestMapping("/api/artisans/me/store")
@Tag(name = "Artisans")
public class ArtisanStoreController {

    private final StoreEditorService editorService;

    public ArtisanStoreController(StoreEditorService editorService) {
        this.editorService = editorService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create the store from a template (draft); 409 if the master already has one")
    public StoreEditorResponse create(@Valid @RequestBody CreateStoreRequest request) {
        return editorService.create(request);
    }

    @GetMapping
    @Operation(summary = "The artisan's store with every section, visible or hidden")
    public StoreEditorResponse mine() {
        return editorService.mine();
    }
}
