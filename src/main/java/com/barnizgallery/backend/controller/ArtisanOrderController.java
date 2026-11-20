package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.ArtisanOrderLineResponse;
import com.barnizgallery.backend.dto.ShipmentRequest;
import com.barnizgallery.backend.service.FulfillmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Paid order lines of the logged-in artisan's store and their shipping. Only for artisans.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Artisans")
public class ArtisanOrderController {

    private final FulfillmentService fulfillmentService;

    public ArtisanOrderController(FulfillmentService fulfillmentService) {
        this.fulfillmentService = fulfillmentService;
    }

    @GetMapping({"/artisans/me/orders", "/artisan/order-items"})
    @Operation(summary = "Paid lines of the store with the shipping address, newest order first")
    public List<ArtisanOrderLineResponse> lines() {
        return fulfillmentService.lines();
    }

    @PostMapping({"/artisans/me/orders/items/{orderItemId}/deliver", "/artisan/order-items/{orderItemId}/delivered"})
    @Operation(summary = "Mark a shipped line as delivered; the order is delivered when all its lines are")
    public ArtisanOrderLineResponse deliver(@PathVariable Long orderItemId) {
        return fulfillmentService.deliver(orderItemId);
    }

    @PostMapping({"/artisans/me/orders/items/{orderItemId}/ship", "/artisan/order-items/{orderItemId}/shipped"})
    @Operation(summary = "Mark a paid line as shipped with carrier and tracking number; the buyer is notified")
    public ArtisanOrderLineResponse ship(@PathVariable Long orderItemId, @Valid @RequestBody ShipmentRequest request) {
        return fulfillmentService.ship(orderItemId, request);
    }
}
