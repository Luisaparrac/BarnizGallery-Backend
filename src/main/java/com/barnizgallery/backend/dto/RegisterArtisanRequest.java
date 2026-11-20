package com.barnizgallery.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Sign-up of a master artisan: the account data plus the public profile of the master. The account starts
 * {@code pendiente} and the artisan goes through the verification steps before opening a store.
 */
public record RegisterArtisanRequest(
        @NotNull @Valid RegisterRequest account,
        @Size(max = 150) String workshop,
        @Size(max = 100) String city,
        @Size(max = 40) String phone,
        @Size(max = 4000) String biographyEs,
        @Size(max = 4000) String biographyEn) {
}
