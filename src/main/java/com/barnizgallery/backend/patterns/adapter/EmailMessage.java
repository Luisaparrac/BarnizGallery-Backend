package com.barnizgallery.backend.patterns.adapter;

/**
 * An email ready to send, already written in the language of the recipient.
 *
 * @param toEmail  recipient address
 * @param toName   recipient name
 * @param subject  subject line
 * @param html     HTML body
 * @param text     plain-text body, for clients that do not show HTML
 */
public record EmailMessage(String toEmail, String toName, String subject, String html, String text) {
}
