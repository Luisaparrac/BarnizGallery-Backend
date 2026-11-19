package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.CustomRequestDesign;

/**
 * Spring Data repository for {@link CustomRequestDesign}.
 */
public interface CustomRequestDesignRepository extends JpaRepository<CustomRequestDesign, Long> {
}
