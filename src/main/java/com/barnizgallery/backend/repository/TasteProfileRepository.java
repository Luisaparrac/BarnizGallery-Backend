package com.barnizgallery.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.model.entity.TasteProfile;

/**
 * Spring Data repository for {@link TasteProfile}.
 */
public interface TasteProfileRepository extends JpaRepository<TasteProfile, Integer> {

    Optional<TasteProfile> findByVisitorVisitorId(Integer visitorId);
}
