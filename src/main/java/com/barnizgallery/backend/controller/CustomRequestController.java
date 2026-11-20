package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.CustomRequestInput;
import com.barnizgallery.backend.dto.CustomRequestResponse;
import com.barnizgallery.backend.service.CustomRequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Custom requests of the logged-in customer.
 */
@RestController
@RequestMapping("/api/custom-requests")
@Tag(name = "Custom orders")
public class CustomRequestController {

    private final CustomRequestService requestService;

    public CustomRequestController(CustomRequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping
    @Operation(summary = "The customer's custom requests, newest first")
    public List<CustomRequestResponse> mine() {
        return requestService.mine();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a draft custom request (catalog ids; shape, style and offered price required)")
    public CustomRequestResponse create(@Valid @RequestBody CustomRequestInput input) {
        return requestService.create(input);
    }

    @GetMapping("/{id}")
    public CustomRequestResponse get(@PathVariable Long id) {
        return requestService.get(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edit a draft custom request")
    public CustomRequestResponse update(@PathVariable Long id, @Valid @RequestBody CustomRequestInput input) {
        return requestService.update(id, input);
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish a draft (description required, design not required)")
    public CustomRequestResponse publish(@PathVariable Long id) {
        return requestService.publish(id);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a draft or open request; its active offers are rejected")
    public CustomRequestResponse cancel(@PathVariable Long id) {
        return requestService.cancel(id);
    }
}
