package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.model.entity.ThreeDModel;

/**
 * Spring Data repository for {@link ThreeDModel}.
 */
public interface ThreeDModelRepository extends JpaRepository<ThreeDModel, Integer> {
}
