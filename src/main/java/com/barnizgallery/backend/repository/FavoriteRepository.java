package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.Favorite;
import com.barnizgallery.backend.entity.FavoriteId;

/**
 * Spring Data repository for {@link Favorite}.
 */
public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {
}
