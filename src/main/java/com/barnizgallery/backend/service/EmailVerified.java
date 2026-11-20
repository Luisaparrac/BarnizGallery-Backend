package com.barnizgallery.backend.service;

import com.barnizgallery.backend.entity.User;

/**
 * Application event: the user proved they own their email (verification link or password reset link).
 */
public record EmailVerified(User user) {
}
