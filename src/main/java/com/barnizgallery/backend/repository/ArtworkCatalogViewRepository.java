package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.ArtworkCatalogView;

/**
 * Spring Data repository for {@link ArtworkCatalogView}.
 */
public interface ArtworkCatalogViewRepository extends JpaRepository<ArtworkCatalogView, Long> {
}
