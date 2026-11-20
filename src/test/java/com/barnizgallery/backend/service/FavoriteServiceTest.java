package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.entity.FavoriteId;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.repository.FavoriteRepository;
import com.barnizgallery.backend.repository.UserRepository;
import com.barnizgallery.backend.security.CurrentUser;

/** Favorites are idempotent and only accept artworks of the public gallery. */
class FavoriteServiceTest {

    private final FavoriteRepository favorites = mock(FavoriteRepository.class);
    private final ArtworkService artworks = mock(ArtworkService.class);
    private final FavoriteService service = new FavoriteService(favorites, mock(UserRepository.class), artworks,
            new CurrentUser());

    @BeforeEach
    void login() {
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject("4").claim("role", "CUSTOMER")
                .issuedAt(now).expiresAt(now.plusSeconds(60)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addingTwiceSavesOnce() {
        when(artworks.requirePublic(9L)).thenReturn(
                TestEntities.artwork(9, TestEntities.store(1, TestEntities.master(1)), ArtworkStatus.EXHIBITED));
        when(favorites.existsById(new FavoriteId(4L, 9L))).thenReturn(false, true);

        service.add(9L);
        service.add(9L);

        verify(favorites, times(1)).save(any());
    }

    @Test
    void artworksOutsideTheGalleryCannotBeAddedAndRemovingAMissingOneIsFine() {
        when(artworks.requirePublic(1L)).thenThrow(new ResourceNotFoundException(ErrorCode.ARTWORK_NOT_FOUND, 1L));

        assertThatThrownBy(() -> service.add(1L)).hasMessage("artwork_not_found [1]");
        service.remove(1L);

        verify(favorites, never()).save(any());
        verify(favorites, never()).deleteById(any());
    }
}
