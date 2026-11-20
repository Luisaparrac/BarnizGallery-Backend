package com.barnizgallery.backend.service;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;

/**
 * Status changes an artisan may make on an artwork: {@code borrador → exhibida → archivada} and back. An artwork in
 * auction or reserved by an order only changes through the auction or the order; a sold artwork can only be
 * archived.
 */
final class ArtworkStatusRules {

    private static final Map<ArtworkStatus, Set<ArtworkStatus>> ALLOWED = Map.of(
            ArtworkStatus.DRAFT, EnumSet.of(ArtworkStatus.EXHIBITED, ArtworkStatus.ARCHIVED),
            ArtworkStatus.EXHIBITED, EnumSet.of(ArtworkStatus.DRAFT, ArtworkStatus.ARCHIVED),
            ArtworkStatus.ARCHIVED, EnumSet.of(ArtworkStatus.DRAFT, ArtworkStatus.EXHIBITED),
            ArtworkStatus.SOLD, EnumSet.of(ArtworkStatus.ARCHIVED),
            ArtworkStatus.IN_AUCTION, EnumSet.noneOf(ArtworkStatus.class),
            ArtworkStatus.RESERVED, EnumSet.noneOf(ArtworkStatus.class));

    private ArtworkStatusRules() {
    }

    /** 409 unless the artisan may move an artwork from one status to the other. */
    static void requireChange(ArtworkStatus from, ArtworkStatus to) {
        if (!ALLOWED.get(from).contains(to)) {
            throw new BusinessRuleException(ErrorCode.ARTWORK_STATUS_CHANGE_INVALID, from, to);
        }
    }
}
