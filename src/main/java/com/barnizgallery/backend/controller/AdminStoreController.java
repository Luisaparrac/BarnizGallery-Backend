package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.AdminPublishStoreRequest;
import com.barnizgallery.backend.dto.AdminStoreResponse;
import com.barnizgallery.backend.service.AdminStoreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Stores of every master. Only for administrators.
 */
@RestController
@RequestMapping("/api/admin/stores")
@Tag(name = "Administration")
public class AdminStoreController {

    private final AdminStoreService storeService;

    public AdminStoreController(AdminStoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping
    @Operation(summary = "Every store with its status, slug and the state of its master")
    public List<AdminStoreResponse> findAll() {
        return storeService.findAll();
    }

    @PostMapping("/{storeId}/publish")
    @Operation(summary = "Publish the store of an active, approved master; { slug } is required if it has none")
    public AdminStoreResponse publish(@PathVariable Long storeId, @Valid @RequestBody AdminPublishStoreRequest request) {
        return storeService.publish(storeId, request.slug());
    }
}
