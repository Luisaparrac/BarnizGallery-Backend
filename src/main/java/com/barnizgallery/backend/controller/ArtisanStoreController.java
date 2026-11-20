package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.CreateStoreRequest;
import com.barnizgallery.backend.dto.SectionOrderRequest;
import com.barnizgallery.backend.dto.SectionVisibilityRequest;
import com.barnizgallery.backend.dto.StoreEditorResponse;
import com.barnizgallery.backend.dto.StoreSectionRequest;
import com.barnizgallery.backend.dto.StoreSectionResponse;
import com.barnizgallery.backend.dto.UpdateStoreRequest;
import com.barnizgallery.backend.service.StoreEditorService;
import com.barnizgallery.backend.service.StoreSectionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Store editor of the logged-in artisan. Only for artisans.
 */
@RestController
@RequestMapping({"/api/artisans/me/store", "/api/artisan/store"})
@Tag(name = "Artisans")
public class ArtisanStoreController {

    private final StoreEditorService editorService;
    private final StoreSectionService sectionService;

    public ArtisanStoreController(StoreEditorService editorService, StoreSectionService sectionService) {
        this.editorService = editorService;
        this.sectionService = sectionService;
    }

    @PostMapping("/sections")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a visible section at the end of the store page")
    public StoreSectionResponse addSection(@Valid @RequestBody StoreSectionRequest request) {
        return sectionService.add(request);
    }

    @PatchMapping("/sections/{sectionId}/visibility")
    @Operation(summary = "Show or hide a section on the public page")
    public StoreSectionResponse setVisibility(@PathVariable Long sectionId,
            @Valid @RequestBody SectionVisibilityRequest request) {
        return sectionService.setVisible(sectionId, request.visible());
    }

    @PutMapping("/sections/order")
    @Operation(summary = "Reorder the sections: every section id of the store, once, from top to bottom")
    public List<StoreSectionResponse> reorderSections(@Valid @RequestBody SectionOrderRequest request) {
        return sectionService.reorder(request.sectionIds());
    }

    @PutMapping("/sections/{sectionId}")
    @Operation(summary = "Replace the content of a section of the artisan's store")
    public StoreSectionResponse updateSection(@PathVariable Long sectionId,
            @Valid @RequestBody StoreSectionRequest request) {
        return sectionService.update(sectionId, request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create the store from a template (draft); 409 if the master already has one")
    public StoreEditorResponse create(@Valid @RequestBody CreateStoreRequest request) {
        return editorService.create(request);
    }

    @RequestMapping(method = { RequestMethod.PATCH, RequestMethod.PUT })
    @Operation(summary = "Change names, descriptions, slug, logo, banner, theme or custom orders (409 if the slug is taken)")
    public StoreEditorResponse update(@Valid @RequestBody UpdateStoreRequest request) {
        return editorService.update(request);
    }

    @PostMapping("/publish")
    @Operation(summary = "Publish the store (409 while the artisan is not approved or the store is suspended)")
    public StoreEditorResponse publish() {
        return editorService.publish();
    }

    @GetMapping
    @Operation(summary = "The artisan's store with every section, visible or hidden")
    public StoreEditorResponse mine() {
        return editorService.mine();
    }
}
