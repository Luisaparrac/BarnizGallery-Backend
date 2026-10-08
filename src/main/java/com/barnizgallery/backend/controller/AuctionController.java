package com.barnizgallery.backend.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.request.BidRequest;
import com.barnizgallery.backend.dto.request.CreateAuctionRequest;
import com.barnizgallery.backend.dto.response.AuctionResponse;
import com.barnizgallery.backend.dto.response.BidResponse;
import com.barnizgallery.backend.model.enums.AuctionStatus;
import com.barnizgallery.backend.service.AuctionService;
import com.barnizgallery.backend.service.BidService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Auctions: creation (Builder pattern), lifecycle (State pattern) and live bids (Observer pattern).
 */
@RestController
@RequestMapping("/api/auctions")
@Tag(name = "Auctions")
public class AuctionController {

    private final AuctionService auctionService;
    private final BidService bidService;

    public AuctionController(AuctionService auctionService, BidService bidService) {
        this.auctionService = auctionService;
        this.bidService = bidService;
    }

    @GetMapping
    public List<AuctionResponse> findAll(@RequestParam(required = false) AuctionStatus status) {
        return auctionService.findAll(status);
    }

    @GetMapping("/{id}")
    public AuctionResponse findById(@PathVariable Integer id) {
        return auctionService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create an auction (basePrice is optional only when AI can suggest one)")
    public ResponseEntity<AuctionResponse> create(@Valid @RequestBody CreateAuctionRequest request) {
        AuctionResponse created = auctionService.create(request);
        return ResponseEntity.created(URI.create("/api/auctions/" + created.auctionId())).body(created);
    }

    @PostMapping("/{id}/start")
    public AuctionResponse start(@PathVariable Integer id) {
        return auctionService.start(id);
    }

    @PostMapping("/{id}/finish")
    public AuctionResponse finish(@PathVariable Integer id) {
        return auctionService.finish(id);
    }

    @PostMapping("/{id}/cancel")
    public AuctionResponse cancel(@PathVariable Integer id) {
        return auctionService.cancel(id);
    }

    @GetMapping("/{id}/bids")
    public List<BidResponse> findBids(@PathVariable Integer id) {
        return bidService.findByAuction(id);
    }

    @PostMapping("/{id}/bids")
    @Operation(summary = "Place a bid (only in active auctions; 422 invalid amount/currency, 429 too many bids)")
    public ResponseEntity<BidResponse> placeBid(@PathVariable Integer id, @Valid @RequestBody BidRequest request) {
        BidResponse created = bidService.placeBid(id, request);
        return ResponseEntity.created(URI.create("/api/auctions/" + id + "/bids")).body(created);
    }
}
