package com.barnizgallery.backend.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.barnizgallery.backend.dto.ArtworkDetailResponse;
import com.barnizgallery.backend.dto.ArtworkRequest;
import com.barnizgallery.backend.dto.ArtworkSummaryResponse;
import com.barnizgallery.backend.dto.PhotoRequest;
import com.barnizgallery.backend.dto.PhotoResponse;
import com.barnizgallery.backend.dto.SuggestedPriceResponse;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.service.ArtworkService;
import com.barnizgallery.backend.service.PhotoService;
import com.barnizgallery.backend.service.PricingService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Artworks and their photos.
 */
@RestController
@RequestMapping("/api/artworks")
@Tag(name = "Artworks")
public class ArtworkController {

    private final ArtworkService artworkService;
    private final PhotoService photoService;
    private final PricingService pricingService;

    public ArtworkController(ArtworkService artworkService, PhotoService photoService,
            PricingService pricingService) {
        this.artworkService = artworkService;
        this.photoService = photoService;
        this.pricingService = pricingService;
    }

    @GetMapping
    public List<ArtworkSummaryResponse> findAll(@RequestParam(required = false) Integer roomId,
            @RequestParam(required = false) ArtworkStatus status) {
        return artworkService.findAll(roomId, status);
    }

    @GetMapping("/{id}")
    public ArtworkDetailResponse findById(@PathVariable Integer id) {
        return artworkService.findById(id);
    }

    @PostMapping
    public ResponseEntity<ArtworkSummaryResponse> create(@Valid @RequestBody ArtworkRequest request) {
        ArtworkSummaryResponse created = artworkService.create(request);
        return ResponseEntity.created(URI.create("/api/artworks/" + created.artworkId())).body(created);
    }

    @PutMapping("/{id}")
    public ArtworkSummaryResponse update(@PathVariable Integer id, @Valid @RequestBody ArtworkRequest request) {
        return artworkService.update(id, request);
    }

    @GetMapping("/{id}/photos")
    public List<PhotoResponse> findPhotos(@PathVariable Integer id) {
        return photoService.findByArtwork(id);
    }

    @PostMapping("/{id}/photos")
    public ResponseEntity<PhotoResponse> addPhoto(@PathVariable Integer id,
            @Valid @RequestBody PhotoRequest request) {
        PhotoResponse created = photoService.addByUrl(id, request);
        return ResponseEntity.created(URI.create("/api/artworks/" + id + "/photos")).body(created);
    }

    /** Multipart upload. Returns 503 while no storage provider is configured. */
    @PostMapping(path = "/{id}/photos/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PhotoResponse> uploadPhoto(@PathVariable Integer id,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String angle) {
        PhotoResponse created = photoService.upload(id, file, angle);
        return ResponseEntity.created(URI.create("/api/artworks/" + id + "/photos")).body(created);
    }

    /** Base price suggested by the AI. Returns 503 while AI is disabled. */
    @GetMapping("/{id}/suggested-price")
    public SuggestedPriceResponse suggestedPrice(@PathVariable Integer id) {
        return pricingService.suggestedPrice(id);
    }
}
