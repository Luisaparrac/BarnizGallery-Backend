package com.barnizgallery.backend.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.StoreDetailResponse;
import com.barnizgallery.backend.dto.StoreRequest;
import com.barnizgallery.backend.dto.StoreSummaryResponse;
import com.barnizgallery.backend.service.StoreService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Stores of the gallery (one per master). There is no DELETE endpoint on purpose.
 */
@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Stores")
public class RoomController {

    private final StoreService storeService;

    public RoomController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping
    public List<StoreSummaryResponse> findAll() {
        return storeService.findAll();
    }

    @GetMapping("/{id}")
    public StoreDetailResponse findById(@PathVariable Long id) {
        return storeService.findById(id);
    }

    @PostMapping
    public ResponseEntity<StoreSummaryResponse> create(@Valid @RequestBody StoreRequest request) {
        StoreSummaryResponse created = storeService.create(request);
        return ResponseEntity.created(URI.create("/api/rooms/" + created.storeId())).body(created);
    }

    @PutMapping("/{id}")
    public StoreSummaryResponse update(@PathVariable Long id, @Valid @RequestBody StoreRequest request) {
        return storeService.update(id, request);
    }
}
