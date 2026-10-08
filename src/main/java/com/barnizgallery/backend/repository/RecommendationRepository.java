package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.entity.Recommendation;

/**
 * Spring Data repository for {@link Recommendation}.
 */
public interface RecommendationRepository extends JpaRepository<Recommendation, Integer> {

    /** Saved recommendations of a visitor with their room, best score first. */
    @Query("select r from Recommendation r join fetch r.room where r.visitor.visitorId = :visitorId "
            + "order by r.score desc nulls last, r.recommendationId")
    List<Recommendation> findByVisitorWithRoom(Integer visitorId);

    /** Removes the previous recommendations of a visitor before saving new ones. */
    @Modifying
    @Query("delete from Recommendation r where r.visitor.visitorId = :visitorId")
    int deleteByVisitorId(Integer visitorId);
}
