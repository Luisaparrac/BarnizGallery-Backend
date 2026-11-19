package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.entity.Recommendation;

/**
 * Spring Data repository for {@link Recommendation}.
 */
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    /** Saved recommendations of a user with their store, best score first. */
    @Query("select r from Recommendation r join fetch r.store where r.user.userId = :userId "
            + "order by r.score desc nulls last, r.recommendationId")
    List<Recommendation> findByUserWithStore(Long userId);

    /** Removes the previous recommendations of a user before saving new ones. */
    @Modifying
    @Query("delete from Recommendation r where r.user.userId = :userId")
    int deleteByUserId(Long userId);
}
