package com.barnizgallery.backend.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
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

    List<Artwork> findByStoreStoreIdAndDeletedAtIsNullAndStatusInOrderByArtworkIdAsc(Long storeId,
            Collection<ArtworkStatus> statuses);

    /** Artworks not deleted with one of the given statuses (the available ones for recommendations). */
    List<Artwork> findByDeletedAtIsNullAndStatusInOrderByArtworkIdAsc(Collection<ArtworkStatus> statuses,
            Pageable page);

    /** Priced artworks with a shape in a currency, except one, to compare prices with (newest first). */
    @Query("select a from Artwork a where a.deletedAt is null and a.price is not null and a.shape is not null "
            + "and a.currency = :currency and a.artworkId <> :artworkId order by a.artworkId desc")
    List<Artwork> findPriceComparables(String currency, Long artworkId, Pageable page);

    /** Artwork count of every store in a single query. */
    @Query("select a.store.storeId as storeId, count(a) as artworkCount from Artwork a group by a.store.storeId")
    List<StoreArtworkCount> countGroupedByStore();
}
