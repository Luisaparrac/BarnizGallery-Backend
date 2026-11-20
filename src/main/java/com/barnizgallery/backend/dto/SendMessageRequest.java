package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Text of a new message.
 */
public record SendMessageRequest(@NotBlank @Size(max = 4000) String content) {
}
