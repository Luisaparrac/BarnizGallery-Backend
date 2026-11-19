package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.StoreSection;

/**
 * Spring Data repository for {@link StoreSection}.
 */
public interface StoreSectionRepository extends JpaRepository<StoreSection, Long> {
}
