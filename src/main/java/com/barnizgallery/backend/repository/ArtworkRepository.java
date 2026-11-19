package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.enums.ArtworkStatus;

/**
 * Spring Data repository for {@link Artwork}.
 */
public interface ArtworkRepository extends JpaRepository<Artwork, Long> {

    List<Artwork> findAllByOrderByArtworkIdAsc();

    List<Artwork> findByStoreStoreIdOrderByArtworkIdAsc(Long storeId);

    List<Artwork> findByStatusOrderByArtworkIdAsc(ArtworkStatus status);

    List<Artwork> findByStoreStoreIdAndStatusOrderByArtworkIdAsc(Long storeId, ArtworkStatus status);

    long countByStoreStoreId(Long storeId);

    /** Artwork count of every store in a single query. */
    @Query("select a.store.storeId as storeId, count(a) as artworkCount from Artwork a group by a.store.storeId")
    List<StoreArtworkCount> countGroupedByStore();
}
