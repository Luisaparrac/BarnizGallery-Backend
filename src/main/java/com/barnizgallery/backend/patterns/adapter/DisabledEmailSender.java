package com.barnizgallery.backend.patterns.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <b>Adapter pattern – default implementation of the Target.</b>
 * <p>
 * Used while the Brevo key or the sender address is empty. Emails are dropped (only the subject is logged, never
 * the links inside, because they carry account tokens); the account flows keep working.
 */
public class DisabledEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(DisabledEmailSender.class);

    @Override
    public void send(EmailMessage message) {
        log.info("Email not sent (email disabled): {}", message.subject());
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
