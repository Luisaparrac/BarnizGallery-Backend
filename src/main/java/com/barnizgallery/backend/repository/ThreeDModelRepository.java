package com.barnizgallery.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.model.entity.ThreeDModel;

/**
 * Spring Data repository for {@link ThreeDModel}.
 */
public interface ThreeDModelRepository extends JpaRepository<ThreeDModel, Integer> {

    Optional<ThreeDModel> findByArtworkArtworkId(Integer artworkId);
}
