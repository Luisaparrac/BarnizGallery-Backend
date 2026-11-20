package com.barnizgallery.backend.patterns.adapter;

/**
 * Sender or recipient in a Brevo transactional email request.
 */
public record BrevoContact(String email, String name) {
}
