package com.barnizgallery.backend.service;

import java.time.Clock;
import java.util.List;
import java.util.Objects;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.ArtworkDetailResponse;
import com.barnizgallery.backend.dto.ArtworkRequest;
import com.barnizgallery.backend.dto.ArtworkSummaryResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.DatabaseErrors;
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
    private final ArtworkService artworkService;

    public ArtisanArtworkService(ArtworkRepository artworkRepository, StoreEditorService editorService,
            CatalogCodeResolver catalogResolver, Clock clock, ArtworkService artworkService) {
        this.artworkService = artworkService;
        this.clock = clock;
        this.artworkRepository = artworkRepository;
        this.editorService = editorService;
        this.catalogResolver = catalogResolver;
    }

    /** Every artwork of the store that is not deleted, drafts included. */
    @Transactional(readOnly = true)
    public List<ArtworkSummaryResponse> mine() {
        return artworkRepository
                .findByStoreStoreIdAndDeletedAtIsNullOrderByArtworkIdAsc(editorService.myStore().getStoreId()).stream()
                .map(ArtworkMapper::toSummary)
                .toList();
    }

    /** Full detail of an artwork of the store, whatever its status. */
    @Transactional(readOnly = true)
    public ArtworkDetailResponse detail(Long artworkId) {
        return artworkService.detail(myArtwork(artworkId));
    }

    @Transactional
    public ArtworkSummaryResponse create(ArtworkRequest request) {
        Artwork artwork = new Artwork();
        artwork.setStore(editorService.myStore());
        artwork.setStatus(ArtworkStatus.DRAFT);
        apply(request, artwork);
        ArtworkSaleRules.validate(artwork);
        return ArtworkMapper.toSummary(artworkRepository.save(artwork));
    }

    /**
     * Replaces the data of an artwork; its status does not change. The database keeps the currency once the
     * artwork has orders or auctions: that rejection is answered with 409.
     */
    @Transactional
    public ArtworkSummaryResponse update(Long artworkId, ArtworkRequest request) {
        Artwork artwork = myArtwork(artworkId);
        String currency = artwork.getCurrency();
        apply(request, artwork);
        ArtworkSaleRules.validate(artwork);
        if (!Objects.equals(currency, artwork.getCurrency())) {
            try {
                artworkRepository.saveAndFlush(artwork);
            } catch (DataAccessException ex) {
                if (DatabaseErrors.isRuleViolation(ex)) {
                    throw new BusinessRuleException(ErrorCode.ARTWORK_CURRENCY_LOCKED, currency);
                }
                throw ex;
            }
        }
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
        ArtworkSaleRules.validate(artwork, status);
        artwork.setStatus(status);
        if (status == ArtworkStatus.EXHIBITED && artwork.getPublishedAt() == null) {
            artwork.setPublishedAt(clock.instant());
        }
        return ArtworkMapper.toSummary(artwork);
    }

    /**
     * Soft delete: the artwork gets {@code deleted_at} and disappears from the store, the gallery and the editor.
     * Orders and auctions keep pointing to it. An artwork in auction or reserved cannot be deleted (409).
     */
    @Transactional
    public void delete(Long artworkId) {
        Artwork artwork = myArtwork(artworkId);
        if (artwork.getStatus() == ArtworkStatus.IN_AUCTION || artwork.getStatus() == ArtworkStatus.RESERVED) {
            throw new BusinessRuleException(ErrorCode.ARTWORK_IN_USE, artwork.getStatus());
        }
        artwork.setDeletedAt(clock.instant());
    }

    Artwork myArtwork(Long artworkId) {
        Store store = editorService.myStore();
        return artworkRepository.findById(artworkId)
                .filter(artwork -> artwork.getDeletedAt() == null
                        && artwork.getStore().getStoreId().equals(store.getStoreId()))
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
