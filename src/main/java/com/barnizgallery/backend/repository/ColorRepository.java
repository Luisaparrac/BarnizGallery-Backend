package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.Color;

/**
 * Spring Data repository for {@link Color}.
 */
public interface ColorRepository extends JpaRepository<Color, Long> {
}
