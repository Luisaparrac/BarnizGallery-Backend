package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.CheckoutRequest;
import com.barnizgallery.backend.dto.OrderResponse;
import com.barnizgallery.backend.dto.OrderStatusChangeResponse;
import com.barnizgallery.backend.service.CheckoutService;
import com.barnizgallery.backend.service.OrderCancelService;
import com.barnizgallery.backend.service.OrderQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Orders of the logged-in buyer.
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders")
public class OrderController {

    private final CheckoutService checkoutService;
    private final OrderQueryService queryService;
    private final OrderCancelService cancelService;

    public OrderController(CheckoutService checkoutService, OrderQueryService queryService,
            OrderCancelService cancelService) {
        this.cancelService = cancelService;
        this.checkoutService = checkoutService;
        this.queryService = queryService;
    }

    @GetMapping
    @Operation(summary = "The buyer's orders, newest first")
    public List<OrderResponse> mine() {
        return queryService.mine();
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel an own order waiting for payment; the reserved stock is released (409 otherwise)")
    public OrderResponse cancel(@PathVariable Long orderId) {
        return cancelService.cancel(orderId);
    }

    @GetMapping("/{orderId}/history")
    @Operation(summary = "Status changes of an own order, oldest first")
    public List<OrderStatusChangeResponse> history(@PathVariable Long orderId) {
        return queryService.history(orderId);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "An order of the buyer with its lines and payment obligations")
    public OrderResponse findById(@PathVariable Long orderId) {
        return queryService.mine(orderId);
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Turn the cart into an order pending payment (409 if empty, mixed currencies, "
            + "not for sale or a setting is missing)")
    public OrderResponse checkout(@Valid @RequestBody CheckoutRequest request) {
        return checkoutService.checkout(request);
    }
}
