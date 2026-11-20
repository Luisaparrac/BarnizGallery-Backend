package com.barnizgallery.backend.security;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.UserRole;
import com.barnizgallery.backend.exception.BusinessRuleException;

/**
 * An artisan may only change their own store, its artworks and their photos; an administrator may change any.
 * The link is {@code masters.user_id}: the master profile that belongs to the logged-in user.
 */
@Component
public class OwnershipGuard {

    private final CurrentUser currentUser;

    public OwnershipGuard(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    /** 403 {@code not_owner} unless the user is an administrator or the master profile is theirs. */
    public void requireMaster(Master master) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        Long userId = currentUser.requireId();
        if (master.getUser() == null || !userId.equals(master.getUser().getUserId())) {
            throw BusinessRuleException.forbidden(ErrorCode.FORBIDDEN);
        }
    }

    /** 403 {@code not_owner} unless the user is an administrator or the store belongs to their master profile. */
    public void requireStore(Store store) {
        requireMaster(store.getMaster());
    }
}
