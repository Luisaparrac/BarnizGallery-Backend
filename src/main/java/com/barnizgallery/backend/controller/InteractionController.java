package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.InteractionRequest;
import com.barnizgallery.backend.dto.InteractionResponse;
import com.barnizgallery.backend.service.InteractionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Interactions of users with artworks (Factory Method pattern).
 */
@RestController
@Tag(name = "Interactions")
public class InteractionController {

    private final InteractionService interactionService;

    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @PostMapping("/api/interactions")
    @PreAuthorize("@access.isSelf(#request.userId())")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record VIEW (durationSeconds required) or ZOOM")
    public InteractionResponse record(@Valid @RequestBody InteractionRequest request) {
        return interactionService.record(request);
    }

    @GetMapping("/api/users/{id}/interactions")
    @PreAuthorize("@access.isSelfOrAdmin(#id)")
    public List<InteractionResponse> history(@PathVariable Long id) {
        return interactionService.history(id);
    }
}
