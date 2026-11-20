package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.ArtisanOrderLineResponse;
import com.barnizgallery.backend.service.FulfillmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Paid order lines of the logged-in artisan's store and their shipping. Only for artisans.
 */
@RestController
@RequestMapping("/api/artisans/me/orders")
@Tag(name = "Artisans")
public class ArtisanOrderController {

    private final FulfillmentService fulfillmentService;

    public ArtisanOrderController(FulfillmentService fulfillmentService) {
        this.fulfillmentService = fulfillmentService;
    }

    @GetMapping
    @Operation(summary = "Paid lines of the store with the shipping address, newest order first")
    public List<ArtisanOrderLineResponse> lines() {
        return fulfillmentService.lines();
    }
}
