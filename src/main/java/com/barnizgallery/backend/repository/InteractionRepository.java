package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.entity.Interaction;

/**
 * Spring Data repository for {@link Interaction}.
 */
public interface InteractionRepository extends JpaRepository<Interaction, Integer> {

    /** History of a visitor, newest first. */
    @Query("select i from Interaction i where i.visitor.visitorId = :visitorId "
            + "order by i.interactionDate desc, i.interactionId desc")
    List<Interaction> findHistory(Integer visitorId);

    /** Interactions of a visitor with the room of each artwork, in a single query. */
    @Query("select i.artwork.room.roomId as roomId, i.action as action, i.durationSeconds as durationSeconds "
            + "from Interaction i where i.visitor.visitorId = :visitorId")
    List<RoomInteraction> findRoomInteractions(Integer visitorId);

    long countByVisitorVisitorId(Integer visitorId);
}
