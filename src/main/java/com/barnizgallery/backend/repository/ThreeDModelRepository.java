package com.barnizgallery.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.ThreeDModel;
import com.barnizgallery.backend.enums.GenerationStatus;

/**
 * Spring Data repository for {@link ThreeDModel}.
 */
public interface ThreeDModelRepository extends JpaRepository<ThreeDModel, Integer> {

    Optional<ThreeDModel> findByArtworkArtworkId(Integer artworkId);

    List<ThreeDModel> findByGenerationStatus(GenerationStatus generationStatus);
}
