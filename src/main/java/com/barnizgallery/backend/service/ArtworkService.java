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
import com.barnizgallery.backend.dto.ThreeDModelResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Room;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.AuctionStatus;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.ArtworkMapper;
import com.barnizgallery.backend.mapper.ThreeDModelMapper;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.AuctionRepository;
import com.barnizgallery.backend.repository.PhotoRepository;
import com.barnizgallery.backend.repository.ThreeDModelRepository;

/**
 * Business logic for artworks. The status of an artwork is never changed here:
 * it is managed by the auction lifecycle.
 */
@Service
public class ArtworkService {

    private final ArtworkRepository artworkRepository;
    private final PhotoRepository photoRepository;
    private final ThreeDModelRepository threeDModelRepository;
    private final AuctionRepository auctionRepository;
    private final AuctionResponseAssembler auctionAssembler;
    private final RoomService roomService;

    public ArtworkService(ArtworkRepository artworkRepository, PhotoRepository photoRepository,
            ThreeDModelRepository threeDModelRepository, AuctionRepository auctionRepository,
            AuctionResponseAssembler auctionAssembler, RoomService roomService) {
        this.artworkRepository = artworkRepository;
        this.photoRepository = photoRepository;
        this.threeDModelRepository = threeDModelRepository;
        this.auctionRepository = auctionRepository;
        this.auctionAssembler = auctionAssembler;
        this.roomService = roomService;
    }

    /** Lists artworks, optionally filtered by room and/or status. */
    @Transactional(readOnly = true)
    public List<ArtworkSummaryResponse> findAll(Integer roomId, ArtworkStatus status) {
        List<Artwork> artworks;
        if (roomId != null && status != null) {
            artworks = artworkRepository.findByRoomRoomIdAndStatusOrderByArtworkIdAsc(roomId, status);
        } else if (roomId != null) {
            artworks = artworkRepository.findByRoomRoomIdOrderByArtworkIdAsc(roomId);
        } else if (status != null) {
            artworks = artworkRepository.findByStatusOrderByArtworkIdAsc(status);
        } else {
            artworks = artworkRepository.findAllByOrderByArtworkIdAsc();
        }
        return artworks.stream().map(ArtworkMapper::toSummary).toList();
    }

    /** Full detail: data, photos, 3D model and current (scheduled or active) auction. */
    @Transactional(readOnly = true)
    public ArtworkDetailResponse findById(Integer artworkId) {
        Artwork artwork = getArtwork(artworkId);
        List<PhotoResponse> photos = photoRepository.findByArtworkArtworkIdOrderByPhotoIdAsc(artworkId).stream()
                .map(ArtworkMapper::toPhotoResponse)
                .toList();
        ThreeDModelResponse model = threeDModelRepository.findByArtworkArtworkId(artworkId)
                .map(ThreeDModelMapper::toResponse)
                .orElse(null);
        AuctionResponse currentAuction = auctionRepository
                .findFirstByArtworkArtworkIdAndStatusInOrderByStartDateDesc(artworkId,
                        EnumSet.of(AuctionStatus.SCHEDULED, AuctionStatus.ACTIVE))
                .map(auctionAssembler::toResponse)
                .orElse(null);
        return ArtworkMapper.toDetail(artwork, photos, model, currentAuction);
    }

    @Transactional
    public ArtworkSummaryResponse create(ArtworkRequest request) {
        Room room = roomService.getRoom(request.roomId());
        Artwork artwork = new Artwork();
        artwork.setRoom(room);
        artwork.setStatus(ArtworkStatus.EXHIBITED);
        ArtworkMapper.apply(request, artwork);
        return ArtworkMapper.toSummary(artworkRepository.save(artwork));
    }

    @Transactional
    public ArtworkSummaryResponse update(Integer artworkId, ArtworkRequest request) {
        Artwork artwork = getArtwork(artworkId);
        if (!artwork.getRoom().getRoomId().equals(request.roomId())) {
            artwork.setRoom(roomService.getRoom(request.roomId()));
        }
        ArtworkMapper.apply(request, artwork);
        return ArtworkMapper.toSummary(artwork);
    }

    /** Loads an artwork or throws 404. Used by other services too. */
    @Transactional(readOnly = true)
    public Artwork getArtwork(Integer artworkId) {
        return artworkRepository.findById(artworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Artwork", artworkId));
    }
}
