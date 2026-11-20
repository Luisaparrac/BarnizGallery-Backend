package com.barnizgallery.backend.service;

import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;

/**
 * Archived masters ({@code masters.is_active = false}, V4) keep their history but take no new operations: stores,
 * artworks, photos, auctions, bids, order lines, offers or conversations. The database enforces it with triggers;
 * the backend checks it first to answer a clear 409.
 */
final class ActiveMasterRule {

    private ActiveMasterRule() {
    }

    static void requireActive(Master master) {
        if (!master.isActive()) {
            throw new BusinessRuleException(ErrorCode.MASTER_ARCHIVED, master.getName());
        }
    }
}
