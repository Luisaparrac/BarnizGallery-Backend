package com.barnizgallery.backend.repository;

import com.barnizgallery.backend.model.enums.InteractionAction;

/**
 * Projection: one interaction reduced to what the recommendation algorithms need
 * ({@link InteractionRepository#findRoomInteractions}).
 */
public interface RoomInteraction {

    Integer getRoomId();

    InteractionAction getAction();

    Integer getDurationSeconds();
}
