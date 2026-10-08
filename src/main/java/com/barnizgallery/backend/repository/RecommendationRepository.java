package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.model.entity.Recommendation;

/**
 * Spring Data repository for {@link Recommendation}.
 */
public interface RecommendationRepository extends JpaRepository<Recommendation, Integer> {
}
