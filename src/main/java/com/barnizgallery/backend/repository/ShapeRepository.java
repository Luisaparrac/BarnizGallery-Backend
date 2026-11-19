package com.barnizgallery.backend.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.Shape;

/**
 * Spring Data repository for {@link Shape}.
 */
public interface ShapeRepository extends JpaRepository<Shape, Long> {

    List<Shape> findByCodeIn(Collection<String> codes);
}
