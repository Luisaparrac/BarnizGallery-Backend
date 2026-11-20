package com.barnizgallery.backend.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Photo;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.MasterVerificationStatus;
import com.barnizgallery.backend.enums.StoreStatus;
import com.barnizgallery.backend.patterns.composite.ArtworkLeaf;
import com.barnizgallery.backend.patterns.composite.GalleryComposite;
import com.barnizgallery.backend.patterns.composite.StoreComposite;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.PhotoRepository;
import com.barnizgallery.backend.repository.StoreRepository;

/**
 * Builds the Composite tree Gallery → Stores → Artworks from the database. Only what the public can see is
 * included: published stores of approved masters and their exhibited or auctioned artworks.
 * <p>
 * It always runs 3 queries (stores with masters, artworks and photos; the primary photo is the thumbnail),
 * whatever the number of stores or artworks, so there is no N+1 problem.
 * Must be called inside a transaction.
 */
@Component
public class GalleryTreeBuilder {

    private final StoreRepository storeRepository;
    private final ArtworkRepository artworkRepository;
    private final PhotoRepository photoRepository;

    public GalleryTreeBuilder(StoreRepository storeRepository, ArtworkRepository artworkRepository,
            PhotoRepository photoRepository) {
        this.storeRepository = storeRepository;
        this.artworkRepository = artworkRepository;
        this.photoRepository = photoRepository;
    }

    public GalleryComposite build() {
        Map<Long, String> thumbnails = photoRepository.findAllByOrderByPrimaryDescSortOrderAscPhotoIdAsc().stream()
                .filter(p -> p.getFileUrl() != null)
                .collect(Collectors.toMap(p -> p.getArtwork().getArtworkId(), Photo::getFileUrl, (first, next) -> first));

        GalleryComposite gallery = new GalleryComposite();
        Map<Long, StoreComposite> stores = new LinkedHashMap<>();
        for (Store store : storeRepository.findByStatusWithMaster(StoreStatus.PUBLISHED,
                MasterVerificationStatus.APPROVED)) {
            StoreComposite node = new StoreComposite(store.getStoreId(), store.getNameEs(), store.getNameEn(),
                    store.getMaster().getMasterId(), store.getMaster().getName());
            stores.put(store.getStoreId(), node);
            gallery.add(node);
        }
        for (Artwork artwork : artworkRepository.findByDeletedAtIsNullAndStatusInOrderByArtworkIdAsc(
                ArtworkStatus.PUBLIC, Pageable.unpaged())) {
            StoreComposite store = stores.get(artwork.getStore().getStoreId());
            if (store != null) {
                store.add(new ArtworkLeaf(artwork.getArtworkId(), artwork.getTitleEs(), artwork.getTitleEn(),
                        artwork.getStatus(), thumbnails.get(artwork.getArtworkId())));
            }
        }
        return gallery;
    }
}
