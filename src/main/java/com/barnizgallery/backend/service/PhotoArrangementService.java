package com.barnizgallery.backend.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.PhotoResponse;
import com.barnizgallery.backend.dto.PhotoUpdateRequest;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Photo;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.ArtworkMapper;
import com.barnizgallery.backend.repository.PhotoRepository;
import com.barnizgallery.backend.security.OwnershipGuard;

/**
 * Alternative texts, order and primary photo of the photos of an artwork. Only the artisan who owns the artwork
 * (or an administrator) may change them.
 */
@Service
public class PhotoArrangementService {

    private final PhotoRepository photoRepository;
    private final ArtworkService artworkService;
    private final OwnershipGuard ownershipGuard;

    public PhotoArrangementService(PhotoRepository photoRepository, ArtworkService artworkService,
            OwnershipGuard ownershipGuard) {
        this.photoRepository = photoRepository;
        this.artworkService = artworkService;
        this.ownershipGuard = ownershipGuard;
    }

    /** Changes texts or angle; {@code primary = true} also takes the primary mark from the other photos. */
    @Transactional
    public PhotoResponse update(Long photoId, PhotoUpdateRequest request) {
        Photo photo = ownedPhoto(photoId);
        if (request.altEs() != null) {
            photo.setAltEs(request.altEs());
        }
        if (request.altEn() != null) {
            photo.setAltEn(request.altEn());
        }
        if (request.angle() != null) {
            photo.setAngle(request.angle());
        }
        if (Boolean.TRUE.equals(request.primary())) {
            photos(photo.getArtwork().getArtworkId()).forEach(other -> other.setPrimary(false));
            photo.setPrimary(true);
        } else if (Boolean.FALSE.equals(request.primary())) {
            photo.setPrimary(false);
        }
        return ArtworkMapper.toPhotoResponse(photo);
    }

    /** Puts the photos in the given order; the list must name every photo of the artwork once (422 otherwise). */
    @Transactional
    public List<PhotoResponse> reorder(Long artworkId, List<Long> photoIds) {
        Artwork artwork = artworkService.getArtwork(artworkId);
        ownershipGuard.requireStore(artwork.getStore());
        Map<Long, Photo> byId = new HashMap<>();
        photos(artworkId).forEach(photo -> byId.put(photo.getPhotoId(), photo));
        if (photoIds.size() != byId.size() || !byId.keySet().equals(new HashSet<>(photoIds))) {
            throw BusinessRuleException.unprocessable(ErrorCode.PHOTO_ORDER_INVALID);
        }
        for (int i = 0; i < photoIds.size(); i++) {
            byId.get(photoIds.get(i)).setSortOrder(i);
        }
        return photoIds.stream().map(byId::get).map(ArtworkMapper::toPhotoResponse).toList();
    }

    /** A photo whose artwork belongs to the current artisan (404 if missing, 403 if not theirs). */
    Photo ownedPhoto(Long photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PHOTO_NOT_FOUND, photoId));
        ownershipGuard.requireStore(photo.getArtwork().getStore());
        return photo;
    }

    List<Photo> photos(Long artworkId) {
        return photoRepository.findByArtworkArtworkIdOrderBySortOrderAscPhotoIdAsc(artworkId);
    }
}
