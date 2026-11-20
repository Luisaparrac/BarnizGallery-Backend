package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.Size;

/**
 * Why an artisan declines a custom request (optional).
 */
public record DeclineRequest(@Size(max = 1000) String reason) {
}
