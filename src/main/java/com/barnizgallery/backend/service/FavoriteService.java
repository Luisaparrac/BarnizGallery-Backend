package com.barnizgallery.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Favorite;
import com.barnizgallery.backend.entity.FavoriteId;
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

    @Transactional
    public void remove(Long artworkId) {
        FavoriteId id = new FavoriteId(currentUser.requireId(), artworkId);
        if (favoriteRepository.existsById(id)) {
            favoriteRepository.deleteById(id);
        }
    }
}
