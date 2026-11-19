package com.barnizgallery.backend.dto;

import java.time.LocalDateTime;

/**
 * A message received by a master.
 */
public record ContactMessageResponse(
        Long messageId,
        Long visitorId,
        String visitorName,
        String visitorEmail,
        Long masterId,
        Long artworkId,
        String content,
        LocalDateTime messageDate) {
}
