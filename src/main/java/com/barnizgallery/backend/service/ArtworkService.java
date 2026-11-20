package com.barnizgallery.backend.service;

import java.util.EnumSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.ArtworkDetailResponse;
import com.barnizgallery.backend.dto.ArtworkSummaryResponse;
import com.barnizgallery.backend.dto.AuctionResponse;
import com.barnizgallery.backend.dto.PhotoResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.AuctionStatus;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.ArtworkMapper;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.AuctionRepository;
import com.barnizgallery.backend.repository.PhotoRepository;

/**
 * Read side of the artworks. Artisans write their own artworks through {@link ArtisanArtworkService}.
 */
@Service
public class ArtworkService {

    private final ArtworkRepository artworkRepository;
    private final PhotoRepository photoRepository;
    private final AuctionRepository auctionRepository;
    private final AuctionResponseAssembler auctionAssembler;

    public ArtworkService(ArtworkRepository artworkRepository, PhotoRepository photoRepository,
            AuctionRepository auctionRepository, AuctionResponseAssembler auctionAssembler) {
        this.artworkRepository = artworkRepository;
        this.photoRepository = photoRepository;
        this.auctionRepository = auctionRepository;
        this.auctionAssembler = auctionAssembler;
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
        List<PhotoResponse> photos = photoRepository.findByArtworkArtworkIdOrderBySortOrderAscPhotoIdAsc(artworkId).stream()
                .map(ArtworkMapper::toPhotoResponse)
                .toList();
        AuctionResponse currentAuction = auctionRepository
                .findFirstByArtworkArtworkIdAndStatusInOrderByStartDateDesc(artworkId,
                        EnumSet.of(AuctionStatus.SCHEDULED, AuctionStatus.ACTIVE))
                .map(auctionAssembler::toResponse)
                .orElse(null);
        return ArtworkMapper.toDetail(artwork, photos, currentAuction);
    }

    /** Loads an artwork that is not deleted or throws 404. Used by other services too. */
    @Transactional(readOnly = true)
    public Artwork getArtwork(Long artworkId) {
        return artworkRepository.findById(artworkId)
                .filter(artwork -> artwork.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ARTWORK_NOT_FOUND, artworkId));
    }
}
