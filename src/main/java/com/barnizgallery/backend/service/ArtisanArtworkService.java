package com.barnizgallery.backend.service;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.ArtworkRequest;
import com.barnizgallery.backend.dto.ArtworkSummaryResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.ArtworkMapper;
import com.barnizgallery.backend.repository.ArtworkRepository;

/**
 * Artworks of the logged-in artisan's store. New artworks start as {@code borrador}; an artwork of another store
 * is answered as not found.
 */
@Service
public class ArtisanArtworkService {

    private final ArtworkRepository artworkRepository;
    private final StoreEditorService editorService;
    private final CatalogCodeResolver catalogResolver;
    private final Clock clock;

    public ArtisanArtworkService(ArtworkRepository artworkRepository, StoreEditorService editorService,
            CatalogCodeResolver catalogResolver, Clock clock) {
        this.clock = clock;
        this.artworkRepository = artworkRepository;
        this.editorService = editorService;
        this.catalogResolver = catalogResolver;
    }

    /** Every artwork of the store, drafts included. */
    @Transactional(readOnly = true)
    public List<ArtworkSummaryResponse> mine() {
        return artworkRepository.findByStoreStoreIdOrderByArtworkIdAsc(editorService.myStore().getStoreId()).stream()
                .map(ArtworkMapper::toSummary)
                .toList();
    }

    @Transactional
    public ArtworkSummaryResponse create(ArtworkRequest request) {
        Artwork artwork = new Artwork();
        artwork.setStore(editorService.myStore());
        artwork.setStatus(ArtworkStatus.DRAFT);
        apply(request, artwork);
        return ArtworkMapper.toSummary(artworkRepository.save(artwork));
    }

    /** Replaces the data of an artwork; its status does not change. */
    @Transactional
    public ArtworkSummaryResponse update(Long artworkId, ArtworkRequest request) {
        Artwork artwork = myArtwork(artworkId);
        apply(request, artwork);
        return ArtworkMapper.toSummary(artwork);
    }

    /**
     * Moves an artwork to another status following {@link ArtworkStatusRules}. The first time it is exhibited its
     * publication date is set.
     */
    @Transactional
    public ArtworkSummaryResponse changeStatus(Long artworkId, ArtworkStatus status) {
        Artwork artwork = myArtwork(artworkId);
        if (artwork.getStatus() == status) {
            return ArtworkMapper.toSummary(artwork);
        }
        ArtworkStatusRules.requireChange(artwork.getStatus(), status);
        if (status == ArtworkStatus.EXHIBITED && artwork.getPublishedAt() == null) {
            artwork.setPublishedAt(clock.instant());
        }
        artwork.setStatus(status);
        return ArtworkMapper.toSummary(artwork);
    }

    Artwork myArtwork(Long artworkId) {
        Store store = editorService.myStore();
        return artworkRepository.findById(artworkId)
                .filter(artwork -> artwork.getStore().getStoreId().equals(store.getStoreId()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ARTWORK_NOT_FOUND, artworkId));
    }

    private void apply(ArtworkRequest request, Artwork artwork) {
        ArtworkMapper.apply(request, artwork);
        artwork.setColors(catalogResolver.colors(request.colorCodes()));
        artwork.setMotifs(catalogResolver.motifs(request.motifCodes()));
        artwork.setShape(catalogResolver.shape(request.shapeCode()));
        artwork.setStyle(catalogResolver.style(request.styleCode()));
    }
}
