package com.barnizgallery.backend.dto.response;

/**
 * Personal notification sent to {@code /topic/visitors/{visitorId}/notifications}.
 *
 * @param type OUTBID or MATCHING_AUCTION
 */
public record VisitorNotification(String type, Integer auctionId, Integer artworkId, String message) {

    public static final String OUTBID = "OUTBID";
    public static final String MATCHING_AUCTION = "MATCHING_AUCTION";
}
