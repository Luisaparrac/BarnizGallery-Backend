package com.barnizgallery.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.barnizgallery.backend.dto.PhotoRequest;
import com.barnizgallery.backend.dto.PhotoResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Photo;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.FeatureDisabledException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.ArtworkMapper;
import com.barnizgallery.backend.patterns.adapter.StorageService;
import com.barnizgallery.backend.patterns.adapter.StoredImage;
import com.barnizgallery.backend.repository.PhotoRepository;
import com.barnizgallery.backend.security.OwnershipGuard;

/**
 * Business logic for artwork photos. Photos are registered by URL or uploaded to the storage provider
 * (upload answers 503 while the storage is not configured).
 */
@Service
public class PhotoService {

    private static final Logger log = LoggerFactory.getLogger(PhotoService.class);

    private final PhotoRepository photoRepository;
    private final ArtworkService artworkService;
    private final StorageService storageService;
    private final ImageUploadValidator imageValidator;
    private final OwnershipGuard ownershipGuard;

    public PhotoService(PhotoRepository photoRepository, ArtworkService artworkService,
            StorageService storageService, ImageUploadValidator imageValidator, OwnershipGuard ownershipGuard) {
        this.ownershipGuard = ownershipGuard;
        this.photoRepository = photoRepository;
        this.artworkService = artworkService;
        this.storageService = storageService;
        this.imageValidator = imageValidator;
    }

    @Transactional(readOnly = true)
    public List<PhotoResponse> findByArtwork(Long artworkId) {
        artworkService.getArtwork(artworkId);
        return photoRepository.findByArtworkArtworkIdOrderBySortOrderAscPhotoIdAsc(artworkId).stream()
                .map(ArtworkMapper::toPhotoResponse)
                .toList();
    }

    @Transactional
    public PhotoResponse addByUrl(Long artworkId, PhotoRequest request) {
        Artwork artwork = artworkService.getArtwork(artworkId);
        ownershipGuard.requireStore(artwork.getStore());
        Photo photo = newPhoto(artwork, request.angle(), request.altEs(), request.altEn());
        photo.setFileUrl(request.fileUrl());
        return ArtworkMapper.toPhotoResponse(photoRepository.save(photo));
    }

    /**
     * Uploads the file through the {@link StorageService} adapter and registers its URL, key and size. The photo
     * goes after the existing ones; the first photo of an artwork is its primary photo.
     */
    @Transactional
    public PhotoResponse upload(Long artworkId, MultipartFile file, String angle, String altEs, String altEn) {
        if (!storageService.isEnabled()) {
            throw new FeatureDisabledException(ErrorCode.STORAGE_DISABLED);
        }
        Artwork artwork = artworkService.getArtwork(artworkId);
        ownershipGuard.requireStore(artwork.getStore());
        imageValidator.validate(file);
        StoredImage image = storageService.upload(file, "artworks/" + artworkId);
        Photo photo = newPhoto(artwork, angle, altEs, altEn);
        photo.setStorageKey(image.storageKey());
        photo.setFileUrl(image.url());
        photo.setWidthPx(image.width());
        photo.setHeightPx(image.height());
        return ArtworkMapper.toPhotoResponse(photoRepository.save(photo));
    }

    /**
     * Deletes the photo. An uploaded photo (with a storage key) is also deleted from the storage; if the storage
     * is not configured any more, only the row is deleted and the file is reported in the log.
     */
    @Transactional
    public void delete(Long photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PHOTO_NOT_FOUND, photoId));
        ownershipGuard.requireStore(photo.getArtwork().getStore());
        photoRepository.delete(photo);
        if (photo.getStorageKey() == null) {
            return;
        }
        if (storageService.isEnabled()) {
            storageService.delete(photo.getStorageKey());
        } else {
            log.warn("Photo {} deleted but its file {} stays in the storage: storage is disabled", photoId,
                    photo.getStorageKey());
        }
    }

    /** A new photo placed after the existing ones; the first photo of an artwork is its primary photo. */
    private Photo newPhoto(Artwork artwork, String angle, String altEs, String altEn) {
        List<Photo> existing = photoRepository
                .findByArtworkArtworkIdOrderBySortOrderAscPhotoIdAsc(artwork.getArtworkId());
        Photo photo = new Photo();
        photo.setArtwork(artwork);
        photo.setAngle(angle);
        photo.setAltEs(altEs);
        photo.setAltEn(altEn);
        photo.setSortOrder(existing.isEmpty() ? 0 : existing.get(existing.size() - 1).getSortOrder() + 1);
        photo.setPrimary(existing.isEmpty());
        return photo;
    }
}
