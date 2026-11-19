package com.barnizgallery.backend.dto;

import java.time.LocalDateTime;

/**
 * A message received by a master.
 */
public record ContactMessageResponse(
        Long messageId,
        Long userId,
        String userName,
        String userEmail,
        Long masterId,
        Long artworkId,
        String content,
        LocalDateTime messageDate) {
}
