package com.barnizgallery.backend.service;

import java.util.EnumSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.ArtworkDetailResponse;
import com.barnizgallery.backend.dto.ArtworkRequest;
import com.barnizgallery.backend.dto.ArtworkSummaryResponse;
import com.barnizgallery.backend.dto.AuctionResponse;
import com.barnizgallery.backend.dto.PhotoResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.AuctionStatus;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.ArtworkMapper;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.AuctionRepository;
import com.barnizgallery.backend.repository.PhotoRepository;
import com.barnizgallery.backend.security.OwnershipGuard;

/**
 * Business logic for artworks. The status of an artwork is never changed here:
 * it is managed by the auction lifecycle.
 */
@Service
public class ArtworkService {

    private final ArtworkRepository artworkRepository;
    private final PhotoRepository photoRepository;
    private final AuctionRepository auctionRepository;
    private final AuctionResponseAssembler auctionAssembler;
    private final StoreService storeService;
    private final CatalogCodeResolver catalogResolver;
    private final OwnershipGuard ownershipGuard;

    public ArtworkService(ArtworkRepository artworkRepository, PhotoRepository photoRepository,
            AuctionRepository auctionRepository, AuctionResponseAssembler auctionAssembler,
            StoreService storeService, CatalogCodeResolver catalogResolver, OwnershipGuard ownershipGuard) {
        this.ownershipGuard = ownershipGuard;
        this.artworkRepository = artworkRepository;
        this.photoRepository = photoRepository;
        this.auctionRepository = auctionRepository;
        this.auctionAssembler = auctionAssembler;
        this.storeService = storeService;
        this.catalogResolver = catalogResolver;
    }

    /** Lists artworks, optionally filtered by store and/or status. */
    @Transactional(readOnly = true)
    public List<ArtworkSummaryResponse> findAll(Long storeId, ArtworkStatus status) {
        List<Artwork> artworks;
        if (storeId != null && status != null) {
            artworks = artworkRepository.findByStoreStoreIdAndStatusOrderByArtworkIdAsc(storeId, status);
        } else if (storeId != null) {
            artworks = artworkRepository.findByStoreStoreIdOrderByArtworkIdAsc(storeId);
        } else if (status != null) {
            artworks = artworkRepository.findByStatusOrderByArtworkIdAsc(status);
        } else {
            artworks = artworkRepository.findAllByOrderByArtworkIdAsc();
        }
        return artworks.stream().map(ArtworkMapper::toSummary).toList();
    }

    /** Full detail: data, photos and current (scheduled or active) auction. */
    @Transactional(readOnly = true)
    public ArtworkDetailResponse findById(Long artworkId) {
        Artwork artwork = getArtwork(artworkId);
        List<PhotoResponse> photos = photoRepository.findByArtworkArtworkIdOrderByPhotoIdAsc(artworkId).stream()
                .map(ArtworkMapper::toPhotoResponse)
                .toList();
        AuctionResponse currentAuction = auctionRepository
                .findFirstByArtworkArtworkIdAndStatusInOrderByStartDateDesc(artworkId,
                        EnumSet.of(AuctionStatus.SCHEDULED, AuctionStatus.ACTIVE))
                .map(auctionAssembler::toResponse)
                .orElse(null);
        return ArtworkMapper.toDetail(artwork, photos, currentAuction);
    }

    @Transactional
    public ArtworkSummaryResponse create(ArtworkRequest request) {
        Store store = storeService.getStore(request.storeId());
        ownershipGuard.requireStore(store);
        Artwork artwork = new Artwork();
        artwork.setStore(store);
        artwork.setStatus(ArtworkStatus.EXHIBITED);
        ArtworkMapper.apply(request, artwork);
        applyCatalogs(request, artwork);
        return ArtworkMapper.toSummary(artworkRepository.save(artwork));
    }

    @Transactional
    public ArtworkSummaryResponse update(Long artworkId, ArtworkRequest request) {
        Artwork artwork = getArtwork(artworkId);
        ownershipGuard.requireStore(artwork.getStore());
        if (!artwork.getStore().getStoreId().equals(request.storeId())) {
            Store newStore = storeService.getStore(request.storeId());
            ownershipGuard.requireStore(newStore);
            artwork.setStore(newStore);
        }
        ArtworkMapper.apply(request, artwork);
        applyCatalogs(request, artwork);
        return ArtworkMapper.toSummary(artwork);
    }

    private void applyCatalogs(ArtworkRequest request, Artwork artwork) {
        artwork.setColors(catalogResolver.colors(request.colorCodes()));
        artwork.setMotifs(catalogResolver.motifs(request.motifCodes()));
    }

    /** Loads an artwork or throws 404. Used by other services too. */
    @Transactional(readOnly = true)
    public Artwork getArtwork(Long artworkId) {
        return artworkRepository.findById(artworkId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ARTWORK_NOT_FOUND, artworkId));
    }
}
