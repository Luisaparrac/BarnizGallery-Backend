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

    /**
     * Interactions of a user reduced to their store, in a single query: the store itself for store-level
     * interactions (a store visit) or the store of the artwork otherwise.
     */
    @Query("select coalesce(st.storeId, artworkStore.storeId) as storeId, i.action as action, "
            + "i.durationSeconds as durationSeconds from Interaction i left join i.store st "
            + "left join i.artwork a left join a.store artworkStore where i.user.userId = :userId")
    List<StoreInteraction> findStoreInteractions(Long userId);

    long countByUserUserId(Long userId);

    /** Interactions of all users with each artwork (its popularity), in a single query. */
    @Query("select i.artwork.artworkId as artworkId, count(i) as interactionCount from Interaction i "
            + "where i.artwork is not null group by i.artwork.artworkId")
    List<ArtworkInteractionCount> countGroupedByArtwork();
}
