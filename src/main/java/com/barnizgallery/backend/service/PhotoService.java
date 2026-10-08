package com.barnizgallery.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.barnizgallery.backend.dto.request.PhotoRequest;
import com.barnizgallery.backend.dto.response.PhotoResponse;
import com.barnizgallery.backend.exception.FeatureDisabledException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.ArtworkMapper;
import com.barnizgallery.backend.model.entity.Artwork;
import com.barnizgallery.backend.model.entity.Photo;
import com.barnizgallery.backend.repository.PhotoRepository;

/**
 * Business logic for artwork photos. Photos are registered by URL;
 * file upload depends on a storage provider that is not configured yet.
 */
@Service
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final ArtworkService artworkService;

    public PhotoService(PhotoRepository photoRepository, ArtworkService artworkService) {
        this.photoRepository = photoRepository;
        this.artworkService = artworkService;
    }

    @Transactional(readOnly = true)
    public List<PhotoResponse> findByArtwork(Integer artworkId) {
        artworkService.getArtwork(artworkId);
        return photoRepository.findByArtworkArtworkIdOrderByPhotoIdAsc(artworkId).stream()
                .map(ArtworkMapper::toPhotoResponse)
                .toList();
    }

    @Transactional
    public PhotoResponse addByUrl(Integer artworkId, PhotoRequest request) {
        Artwork artwork = artworkService.getArtwork(artworkId);
        Photo photo = new Photo();
        photo.setArtwork(artwork);
        photo.setFileUrl(request.fileUrl());
        photo.setAngle(request.angle());
        return ArtworkMapper.toPhotoResponse(photoRepository.save(photo));
    }

    @Transactional
    public PhotoResponse upload(Integer artworkId, MultipartFile file, String angle) {
        throw new FeatureDisabledException("Photo storage is not configured; register photos by URL instead");
    }

    @Transactional
    public void delete(Integer photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo", photoId));
        photoRepository.delete(photo);
    }
}
