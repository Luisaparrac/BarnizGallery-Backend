package com.barnizgallery.backend.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.CartAddRequest;
import com.barnizgallery.backend.dto.CartItemRequest;
import com.barnizgallery.backend.dto.CartResponse;
import com.barnizgallery.backend.service.CartService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * The cart of the logged-in user.
 */
@RestController
@RequestMapping("/api/cart")
@Tag(name = "Orders")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @Operation(summary = "Cart lines with current prices, availability and subtotals per currency")
    public CartResponse view() {
        return cartService.view();
    }

    @PostMapping("/items")
    @Operation(summary = "Add units of an artwork to the cart ({ artworkId, quantity = 1 })")
    public CartResponse add(@Valid @RequestBody CartAddRequest request) {
        return cartService.add(request.artworkId(), request.quantity() == null ? 1 : request.quantity());
    }

    @PutMapping("/items/{artworkId}")
    @Operation(summary = "Put an artwork for sale in the cart with a quantity (409 if not for sale or not enough stock)")
    public CartResponse put(@PathVariable Long artworkId, @Valid @RequestBody CartItemRequest request) {
        return cartService.put(artworkId, request.quantity());
    }

    @DeleteMapping("/items/{artworkId}")
    @Operation(summary = "Remove an artwork from the cart")
    public CartResponse remove(@PathVariable Long artworkId) {
        return cartService.remove(artworkId);
    }
}
