package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Reference of the bank transfer (or other means) that paid a payout.
 */
public record PayoutPaidRequest(@NotBlank @Size(max = 200) String reference) {
}
