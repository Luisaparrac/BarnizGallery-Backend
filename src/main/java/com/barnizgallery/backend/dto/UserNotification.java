package com.barnizgallery.backend.dto;

/**
 * Personal notification sent to {@code /topic/users/{userId}/notifications}.
 *
 * @param type OUTBID or MATCHING_AUCTION
 */
public record UserNotification(String type, Long auctionId, Long artworkId, String message) {

    public static final String OUTBID = "OUTBID";
    public static final String MATCHING_AUCTION = "MATCHING_AUCTION";
}
