package com.barnizgallery.backend.dto;

/** Body of {@code POST /v1/bids/assess} (AI service 1.3). */
public record AiBidAssessRequest(AiBidAuction auction, AiBidPlaced bid, AiBidder bidder) {
}
