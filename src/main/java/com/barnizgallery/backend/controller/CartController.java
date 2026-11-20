package com.barnizgallery.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.CartResponse;
import com.barnizgallery.backend.service.CartService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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
}
