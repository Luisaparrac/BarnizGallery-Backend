package com.barnizgallery.backend.dto;

import java.time.Instant;

import com.barnizgallery.backend.enums.MasterVerificationStatus;
import com.barnizgallery.backend.enums.StoreStatus;

/**
 * A store as administrators see it, with the state of its master.
 */
public record AdminStoreResponse(Long storeId, String slug, String nameEs, String nameEn, StoreStatus status,
        Instant publishedAt, Long masterId, String masterName, MasterVerificationStatus masterVerification,
        boolean masterActive) {
}
