package com.barnizgallery.backend.patterns.strategy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.repository.ArtworkCatalogViewRepository;
import com.barnizgallery.backend.repository.ArtworkRepository;

/**
 * The artworks every recommendation strategy may suggest: only those of the public catalog
 * ({@code v_artwork_catalog}: exhibited or in auction, published store, approved and active master), at most
 * {@value AiServiceStrategy#MAX_CANDIDATES}.
 */
@Component
public class PublicArtworkCatalog {

    private final ArtworkRepository artworkRepository;
    private final ArtworkCatalogViewRepository catalogRepository;

    public PublicArtworkCatalog(ArtworkRepository artworkRepository, ArtworkCatalogViewRepository catalogRepository) {
        this.artworkRepository = artworkRepository;
        this.catalogRepository = catalogRepository;
    }

    public List<Artwork> artworks() {
        Set<Long> publicIds = new HashSet<>(catalogRepository.findAllArtworkIds());
        if (publicIds.isEmpty()) {
            return List.of();
        }
        return artworkRepository.findByDeletedAtIsNullAndStatusInOrderByArtworkIdAsc(ArtworkStatus.PUBLIC,
                AiServiceStrategy.CANDIDATES).stream()
                .filter(artwork -> publicIds.contains(artwork.getArtworkId()))
                .toList();
    }
}
