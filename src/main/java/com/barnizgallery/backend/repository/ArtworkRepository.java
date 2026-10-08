package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.model.entity.Artwork;

/**
 * Spring Data repository for {@link Artwork}.
 */
public interface ArtworkRepository extends JpaRepository<Artwork, Integer> {
}
