package com.barnizgallery.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.ArtworkSummaryResponse;
import com.barnizgallery.backend.dto.FavoriteToggleResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Favorite;
import com.barnizgallery.backend.entity.FavoriteId;
import com.barnizgallery.backend.mapper.ArtworkMapper;
import com.barnizgallery.backend.repository.FavoriteRepository;
import com.barnizgallery.backend.repository.UserRepository;
import com.barnizgallery.backend.security.CurrentUser;

/**
 * Favorite artworks of the logged-in user. Adding and removing are idempotent; only artworks of the public
 * gallery can be added.
 */
@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ArtworkService artworkService;
    private final CurrentUser currentUser;

    public FavoriteService(FavoriteRepository favoriteRepository, UserRepository userRepository,
            ArtworkService artworkService, CurrentUser currentUser) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.artworkService = artworkService;
        this.currentUser = currentUser;
    }

    /**
     * Favorite artworks, last saved first. Artworks that left the gallery (sold, archived...) stay listed with
     * their current status; deleted ones are left out.
     */
    @Transactional(readOnly = true)
    public List<ArtworkSummaryResponse> mine() {
        return favoriteRepository.findArtworksOfUser(currentUser.requireId()).stream()
                .map(ArtworkMapper::toSummary)
                .toList();
    }

    @Transactional
    public void add(Long artworkId) {
        Long userId = currentUser.requireId();
        Artwork artwork = artworkService.requirePublic(artworkId);
        if (favoriteRepository.existsById(new FavoriteId(userId, artworkId))) {
            return;
        }
        Favorite favorite = new Favorite();
        favorite.setUser(userRepository.getReferenceById(userId));
        favorite.setArtwork(artwork);
        favoriteRepository.save(favorite);
    }

    /** Adds the artwork when it is not a favorite and removes it when it is. */
    @Transactional
    public FavoriteToggleResponse toggle(Long artworkId) {
        if (favoriteRepository.existsById(new FavoriteId(currentUser.requireId(), artworkId))) {
            remove(artworkId);
            return new FavoriteToggleResponse(false);
        }
        add(artworkId);
        return new FavoriteToggleResponse(true);
    }

    @Transactional
    public void remove(Long artworkId) {
        FavoriteId id = new FavoriteId(currentUser.requireId(), artworkId);
        if (favoriteRepository.existsById(id)) {
            favoriteRepository.deleteById(id);
        }
    }
}
