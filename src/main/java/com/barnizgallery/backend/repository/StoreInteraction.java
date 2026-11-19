package com.barnizgallery.backend.repository;

import com.barnizgallery.backend.enums.InteractionAction;

/**
 * Projection: one interaction reduced to what the recommendation algorithms need
 * ({@link InteractionRepository#findStoreInteractions}).
 */
public interface StoreInteraction {

    Long getStoreId();

    InteractionAction getAction();

    Integer getDurationSeconds();
}
