package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Favorite;
import com.barnizgallery.backend.entity.FavoriteId;

/**
 * Spring Data repository for {@link Favorite}.
 */
public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {

    /** Favorite artworks of a user that are not deleted, last saved first. */
    @Query("select f.artwork from Favorite f where f.user.userId = :userId and f.artwork.deletedAt is null "
            + "order by f.createdAt desc")
    List<Artwork> findArtworksOfUser(Long userId);
}
