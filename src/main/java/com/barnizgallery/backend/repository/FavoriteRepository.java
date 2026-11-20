package com.barnizgallery.backend.repository;

import java.util.Collection;
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

    /** How many users saved each of these artworks, in a single query. */
    @Query("select f.artwork.artworkId as artworkId, count(f) as total from Favorite f "
            + "where f.artwork.artworkId in :artworkIds group by f.artwork.artworkId")
    List<ArtworkFavoriteCount> countByArtworks(Collection<Long> artworkIds);

    long countByArtworkArtworkId(Long artworkId);

    /** Favorite artworks of a user that are not deleted, last saved first. */
    @Query("select f.artwork from Favorite f where f.user.userId = :userId and f.artwork.deletedAt is null "
            + "order by f.createdAt desc")
    List<Artwork> findArtworksOfUser(Long userId);
}
