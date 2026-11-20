package com.barnizgallery.backend.service;

import java.util.EnumSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.ArtworkDetailResponse;
import com.barnizgallery.backend.dto.AuctionResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.FavoriteId;
import com.barnizgallery.backend.entity.Photo;
import com.barnizgallery.backend.enums.AuctionStatus;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.ArtworkMapper;
import com.barnizgallery.backend.repository.ArtworkCatalogViewRepository;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.AuctionRepository;
import com.barnizgallery.backend.repository.FavoriteRepository;
import com.barnizgallery.backend.repository.PhotoRepository;
import com.barnizgallery.backend.security.CurrentUser;

/**
 * Read side of the artworks. Artisans write their own artworks through {@link ArtisanArtworkService}.
 */
@Service
public class ArtworkService {

    private final ArtworkRepository artworkRepository;
    private final PhotoRepository photoRepository;
    private final AuctionRepository auctionRepository;
    private final AuctionResponseAssembler auctionAssembler;
    private final ArtworkCatalogViewRepository catalogRepository;
    private final FavoriteRepository favoriteRepository;
    private final CurrentUser currentUser;

    public ArtworkService(ArtworkRepository artworkRepository, PhotoRepository photoRepository,
            AuctionRepository auctionRepository, AuctionResponseAssembler auctionAssembler,
            ArtworkCatalogViewRepository catalogRepository, FavoriteRepository favoriteRepository,
            CurrentUser currentUser) {
        this.favoriteRepository = favoriteRepository;
        this.currentUser = currentUser;
        this.catalogRepository = catalogRepository;
        this.artworkRepository = artworkRepository;
        this.photoRepository = photoRepository;
        this.auctionRepository = auctionRepository;
        this.auctionAssembler = auctionAssembler;
    }

    /**
     * Public detail: data, photos and current (scheduled or active) auction. Only artworks of the public gallery
     * exist for the public (404 otherwise).
     */
    @Transactional(readOnly = true)
    public ArtworkDetailResponse findById(Long artworkId) {
        return detail(requirePublic(artworkId));
    }

    /** Public detail by id or, as the frontend asks, by slug (404 unless it is in the public gallery). */
    @Transactional(readOnly = true)
    public ArtworkDetailResponse findByIdOrSlug(String idOrSlug) {
        if (idOrSlug.chars().allMatch(Character::isDigit)) {
            return findById(Long.valueOf(idOrSlug));
        }
        Artwork artwork = artworkRepository.findFirstBySlugAndDeletedAtIsNull(idOrSlug)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ARTWORK_NOT_FOUND, idOrSlug));
        return findById(artwork.getArtworkId());
    }

    /** 404 unless the artwork is in the public gallery. */
    @Transactional(readOnly = true)
    public Artwork requirePublic(Long artworkId) {
        if (!catalogRepository.existsById(artworkId)) {
            throw new ResourceNotFoundException(ErrorCode.ARTWORK_NOT_FOUND, artworkId);
        }
        return getArtwork(artworkId);
    }

    /** Data, photos in order and current (scheduled or active) auction of an artwork. */
    @Transactional(readOnly = true)
    public ArtworkDetailResponse detail(Artwork artwork) {
        Long artworkId = artwork.getArtworkId();
        List<Photo> photos = photoRepository.findByArtworkArtworkIdOrderBySortOrderAscPhotoIdAsc(artworkId);
        AuctionResponse currentAuction = auctionRepository
                .findFirstByArtworkArtworkIdAndStatusInOrderByStartDateDesc(artworkId,
                        EnumSet.of(AuctionStatus.SCHEDULED, AuctionStatus.ACTIVE))
                .map(auctionAssembler::toResponse)
                .orElse(null);
        boolean favorite = currentUser.id()
                .map(userId -> favoriteRepository.existsById(new FavoriteId(userId, artworkId)))
                .orElse(false);
        return ArtworkMapper.toDetail(artwork, photos, currentAuction,
                favoriteRepository.countByArtworkArtworkId(artworkId), favorite);
    }

    /** Loads an artwork that is not deleted or throws 404. Used by other services too. */
    @Transactional(readOnly = true)
    public Artwork getArtwork(Long artworkId) {
        return artworkRepository.findById(artworkId)
                .filter(artwork -> artwork.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ARTWORK_NOT_FOUND, artworkId));
    }
}
