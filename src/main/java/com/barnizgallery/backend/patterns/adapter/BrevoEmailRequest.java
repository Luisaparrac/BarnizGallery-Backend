package com.barnizgallery.backend.patterns.adapter;

import java.util.List;

/**
 * Body of Brevo's {@code POST /v3/smtp/email} (transactional email).
 */
public record BrevoEmailRequest(BrevoContact sender, List<BrevoContact> to, String subject, String htmlContent,
        String textContent) {
}
