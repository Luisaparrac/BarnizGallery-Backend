package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.Style;

/**
 * Spring Data repository for {@link Style}.
 */
public interface StyleRepository extends JpaRepository<Style, Long> {
}
