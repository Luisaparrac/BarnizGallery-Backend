package com.barnizgallery.backend.security;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.enums.UserRole;

/**
 * Data-dependent access rules, used in {@code @PreAuthorize} expressions as {@code @access}. Ids sent in the
 * path or in the body are never trusted: they must belong to the logged-in user.
 */
@Component("access")
public class AccessGuard {

    private final CurrentUser currentUser;

    public AccessGuard(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    /** True when the id is the logged-in user. */
    public boolean isSelf(Long userId) {
        return userId != null && currentUser.id().filter(userId::equals).isPresent();
    }

    /** True when the id is the logged-in user, or the logged-in user is an administrator. */
    public boolean isSelfOrAdmin(Long userId) {
        return currentUser.hasRole(UserRole.ADMIN) || isSelf(userId);
    }
}
