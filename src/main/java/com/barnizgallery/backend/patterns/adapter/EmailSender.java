package com.barnizgallery.backend.patterns.adapter;

/**
 * <b>Adapter pattern – Target.</b>
 * <p>
 * The interface the system uses to send emails (account verification and password reset). Render's free plan
 * blocks SMTP ports, so emails go through an HTTP API; an adapter per provider (Brevo today) translates this
 * call into its API.
 */
public interface EmailSender {

    /** Sends the email. A provider failure must not break the user's flow: adapters log it instead. */
    void send(EmailMessage message);

    boolean isEnabled();
}
