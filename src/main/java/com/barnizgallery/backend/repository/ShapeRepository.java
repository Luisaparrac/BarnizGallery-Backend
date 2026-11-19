package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.Shape;

/**
 * Spring Data repository for {@link Shape}.
 */
public interface ShapeRepository extends JpaRepository<Shape, Long> {
}
