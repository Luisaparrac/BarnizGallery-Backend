package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.model.entity.Interaction;

/**
 * Spring Data repository for {@link Interaction}.
 */
public interface InteractionRepository extends JpaRepository<Interaction, Integer> {

    /** History of a visitor, newest first. */
    @Query("select i from Interaction i where i.visitor.visitorId = :visitorId "
            + "order by i.interactionDate desc, i.interactionId desc")
    List<Interaction> findHistory(Integer visitorId);
}
