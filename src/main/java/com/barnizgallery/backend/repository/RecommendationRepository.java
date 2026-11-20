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

    /** Saved recommendations of a user (stores and artworks), best score first. */
    @Query("select r from Recommendation r left join fetch r.store left join fetch r.artwork "
            + "where r.user.userId = :userId order by r.score desc nulls last, r.recommendationId")
    List<Recommendation> findByUserWithTarget(Long userId);

    /** Removes the previous store recommendations of a user before saving new ones. */
    @Modifying
    @Query("delete from Recommendation r where r.user.userId = :userId and r.store is not null")
    int deleteStoresByUserId(Long userId);

    /** Removes the previous artwork recommendations of a user before saving new ones. */
    @Modifying
    @Query("delete from Recommendation r where r.user.userId = :userId and r.artwork is not null")
    int deleteArtworksByUserId(Long userId);
}
