package com.barnizgallery.backend.dto;

import java.time.LocalDateTime;

/**
 * A message received by a master.
 */
public record ContactMessageResponse(
        Integer messageId,
        Integer visitorId,
        String visitorName,
        String visitorEmail,
        Integer masterId,
        Integer artworkId,
        String content,
        LocalDateTime messageDate) {
}
