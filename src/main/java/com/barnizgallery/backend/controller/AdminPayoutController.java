package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.PayoutPaidRequest;
import com.barnizgallery.backend.dto.PayoutResponse;
import com.barnizgallery.backend.service.PayoutService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Payouts to artisans. Only for administrators.
 */
@RestController
@RequestMapping("/api/admin/payouts")
@Tag(name = "Administration")
public class AdminPayoutController {

    private final PayoutService payoutService;

    public AdminPayoutController(PayoutService payoutService) {
        this.payoutService = payoutService;
    }

    @GetMapping
    @Operation(summary = "Every payout, newest first")
    public List<PayoutResponse> findAll() {
        return payoutService.findAll();
    }

    @PostMapping("/{payoutId}/paid")
    @Operation(summary = "Mark a pending payout as paid with its transfer reference; the artisan is notified")
    public PayoutResponse markPaid(@PathVariable Long payoutId, @Valid @RequestBody PayoutPaidRequest request) {
        return payoutService.markPaid(payoutId, request.reference());
    }

    @PostMapping
    @Operation(summary = "Create pending payouts from delivered lines without one (one per master and currency)")
    public List<PayoutResponse> create() {
        return payoutService.createFromDeliveredLines();
    }
}
