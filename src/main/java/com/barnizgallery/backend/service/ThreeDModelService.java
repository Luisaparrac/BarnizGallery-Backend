package com.barnizgallery.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.ThreeDModelResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Photo;
import com.barnizgallery.backend.entity.ThreeDModel;
import com.barnizgallery.backend.enums.GenerationStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.FeatureDisabledException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.ThreeDModelMapper;
import com.barnizgallery.backend.patterns.adapter.GenerationTicket;
import com.barnizgallery.backend.patterns.adapter.ThreeDModelGenerator;
import com.barnizgallery.backend.repository.PhotoRepository;
import com.barnizgallery.backend.repository.ThreeDModelRepository;

/**
 * 3D models of artworks, generated from their photos through the {@link ThreeDModelGenerator}
 * adapter (Hyper3D Rodin). Generation is done only once per artwork.
 */
@Service
public class ThreeDModelService {

    private final ThreeDModelRepository threeDModelRepository;
    private final PhotoRepository photoRepository;
    private final ArtworkService artworkService;
    private final ThreeDModelGenerator generator;

    public ThreeDModelService(ThreeDModelRepository threeDModelRepository, PhotoRepository photoRepository,
            ArtworkService artworkService, ThreeDModelGenerator generator) {
        this.threeDModelRepository = threeDModelRepository;
        this.photoRepository = photoRepository;
        this.artworkService = artworkService;
        this.generator = generator;
    }

    @Transactional(readOnly = true)
    public ThreeDModelResponse findByArtwork(Integer artworkId) {
        artworkService.getArtwork(artworkId);
        return ThreeDModelMapper.toResponse(getModel(artworkId));
    }

    /** Sends the artwork photos to Hyper3D and stores the task as "processing". */
    @Transactional
    public ThreeDModelResponse generate(Integer artworkId) {
        Artwork artwork = artworkService.getArtwork(artworkId);
        requireEnabled();
        ThreeDModel model = threeDModelRepository.findByArtworkArtworkId(artworkId).orElse(null);
        if (model != null && model.getGenerationStatus() == GenerationStatus.COMPLETED) {
            throw new BusinessRuleException("The 3D model of artwork " + artworkId + " is already completed");
        }
        List<String> photoUrls = photoRepository.findByArtworkArtworkIdOrderByPhotoIdAsc(artworkId).stream()
                .map(Photo::getFileUrl)
                .toList();
        if (photoUrls.isEmpty()) {
            throw BusinessRuleException.unprocessable("Artwork " + artworkId + " needs at least one photo");
        }

        GenerationTicket ticket = generator.submit(photoUrls);
        if (model == null) {
            model = new ThreeDModel();
            model.setArtwork(artwork);
        }
        model.setHyper3dTaskId(ticket.encode());
        model.setGenerationStatus(GenerationStatus.PROCESSING);
        model.setGlbFileUrl(null);
        model.setGenerationDate(LocalDateTime.now());
        return ThreeDModelMapper.toResponse(threeDModelRepository.save(model));
    }

    /** Asks Hyper3D for the status; when it is done, stores the GLB URL. */
    @Transactional
    public ThreeDModelResponse refresh(Integer artworkId) {
        artworkService.getArtwork(artworkId);
        requireEnabled();
        ThreeDModel model = getModel(artworkId);
        if (model.getGenerationStatus() == GenerationStatus.COMPLETED) {
            return ThreeDModelMapper.toResponse(model);
        }
        if (model.getHyper3dTaskId() == null || model.getHyper3dTaskId().isBlank()) {
            throw new BusinessRuleException("The 3D model of artwork " + artworkId + " has no Hyper3D task");
        }
        GenerationTicket ticket = GenerationTicket.decode(model.getHyper3dTaskId());
        GenerationStatus status = generator.checkStatus(ticket);
        if (status == GenerationStatus.COMPLETED) {
            generator.fetchGlbUrl(ticket).ifPresentOrElse(url -> {
                model.setGlbFileUrl(url);
                model.setGenerationStatus(GenerationStatus.COMPLETED);
            }, () -> model.setGenerationStatus(GenerationStatus.PROCESSING));
        } else {
            model.setGenerationStatus(status);
        }
        return ThreeDModelMapper.toResponse(model);
    }

    private ThreeDModel getModel(Integer artworkId) {
        return threeDModelRepository.findByArtworkArtworkId(artworkId)
                .orElseThrow(() -> new ResourceNotFoundException("3D model of artwork " + artworkId + " not found"));
    }

    private void requireEnabled() {
        if (!generator.isEnabled()) {
            throw new FeatureDisabledException("Hyper3D is disabled: HYPER3D_API_KEY is not configured");
        }
    }
}
