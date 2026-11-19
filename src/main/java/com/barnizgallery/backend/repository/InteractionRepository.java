package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.entity.Interaction;

/**
 * Spring Data repository for {@link Interaction}.
 */
public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    /** History of a user, newest first. */
    @Query("select i from Interaction i where i.user.userId = :userId "
            + "order by i.interactionDate desc, i.interactionId desc")
    List<Interaction> findHistory(Long userId);

    /** Interactions of a user with the room of each artwork, in a single query. */
    @Query("select i.artwork.room.roomId as roomId, i.action as action, i.durationSeconds as durationSeconds "
            + "from Interaction i where i.user.userId = :userId")
    List<RoomInteraction> findRoomInteractions(Long userId);

    long countByUserUserId(Long userId);
}
