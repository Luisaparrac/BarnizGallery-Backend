package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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
 * Interactions of visitors with artworks (Factory Method pattern).
 */
@RestController
@Tag(name = "Interactions")
public class InteractionController {

    private final InteractionService interactionService;

    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @PostMapping("/api/interactions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record VIEW (durationSeconds required), TOUCH or ROTATE")
    public InteractionResponse record(@Valid @RequestBody InteractionRequest request) {
        return interactionService.record(request);
    }

    @GetMapping("/api/visitors/{id}/interactions")
    public List<InteractionResponse> history(@PathVariable Integer id) {
        return interactionService.history(id);
    }
}
