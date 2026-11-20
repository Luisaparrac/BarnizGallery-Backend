package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.AdminArtworkUpdateRequest;
import com.barnizgallery.backend.entity.AppSetting;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.MasterVerificationStatus;
import com.barnizgallery.backend.enums.SaleMode;
import com.barnizgallery.backend.enums.StoreStatus;
import com.barnizgallery.backend.repository.AppSettingRepository;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.MasterRepository;
import com.barnizgallery.backend.repository.MasterVerificationRepository;
import com.barnizgallery.backend.repository.StoreRepository;
import com.barnizgallery.backend.repository.UserRepository;
import com.barnizgallery.backend.security.CurrentUser;

import tools.jackson.databind.json.JsonMapper;

/** The administrator's tools to prepare the demo: settings, roster masters, stores and artwork sale data. */
class AdminDemoToolsTest {

    private static final Instant NOW = Instant.parse("2026-11-20T12:00:00Z");

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final CurrentUser currentUser = mock(CurrentUser.class);
    private final UserRepository users = mock(UserRepository.class);

    @Test
    void aSettingIsChangedByTheAdministratorAndTheCacheIsCleared() {
        AppSettingRepository repository = mock(AppSettingRepository.class);
        AppSettingsService settings = mock(AppSettingsService.class);
        AppSetting minutes = new AppSetting();
        minutes.setSettingKey("cart.reservation_minutes");
        minutes.setValue("null");
        when(repository.findById("cart.reservation_minutes")).thenReturn(Optional.of(minutes));
        when(currentUser.requireId()).thenReturn(1L);
        AppSettingsAdminService service = new AppSettingsAdminService(repository, settings, users, currentUser, clock);

        assertThat(service.update("cart.reservation_minutes", JsonMapper.builder().build().readTree("15")).value())
                .isEqualTo("15");
        verify(settings).invalidate();
        assertThatThrownBy(() -> service.update("nada", JsonMapper.builder().build().readTree("1")))
                .hasMessage("setting_not_found [nada]");
    }

    @Test
    void rosterMastersAreApprovedDirectlyButArchivedOnesAreNot() {
        MasterRepository masters = mock(MasterRepository.class);
        MasterVerificationRepository steps = mock(MasterVerificationRepository.class);
        Master roster = TestEntities.master(7);
        Master archived = TestEntities.master(2);
        archived.setActive(false);
        when(masters.findById(7L)).thenReturn(Optional.of(roster));
        when(masters.findById(2L)).thenReturn(Optional.of(archived));
        when(steps.findByMasterMasterIdOrderByVerificationIdAsc(7L)).thenReturn(List.of());
        when(currentUser.requireId()).thenReturn(1L);
        AdminMasterService service = new AdminMasterService(masters, steps, mock(VerificationReviewService.class),
                users, currentUser, clock);

        assertThat(service.approve(7L).verificationStatus()).isEqualTo(MasterVerificationStatus.APPROVED);
        assertThat(roster.getVerifiedAt()).isEqualTo(NOW);
        assertThatThrownBy(() -> service.approve(2L)).hasMessage("master_archived [Test master 2]");
    }

    @Test
    void aStoreIsPublishedWithItsSlugOnlyForAnApprovedMaster() {
        StoreRepository stores = mock(StoreRepository.class);
        Store store = TestEntities.store(10, TestEntities.master(7));
        when(stores.findById(10L)).thenReturn(Optional.of(store));
        AdminStoreService service = new AdminStoreService(stores, clock);

        assertThatThrownBy(() -> service.publish(10L, "granja")).hasMessage("store_master_not_approved");
        store.getMaster().setVerificationStatus(MasterVerificationStatus.APPROVED);
        assertThatThrownBy(() -> service.publish(10L, null)).hasMessage("store_slug_required");

        assertThat(service.publish(10L, "granja").status()).isEqualTo(StoreStatus.PUBLISHED);
        assertThat(store.getSlug()).isEqualTo("granja");
        assertThat(store.getPublishedAt()).isEqualTo(NOW);
    }

    @Test
    void artworkSaleDataFollowsTheSameRules() {
        ArtworkRepository artworks = mock(ArtworkRepository.class);
        ArtworkService artworkService = mock(ArtworkService.class);
        Artwork artwork = TestEntities.artwork(30, TestEntities.store(10, TestEntities.master(7)), ArtworkStatus.DRAFT);
        artwork.setSaleMode(SaleMode.AUCTION);
        when(artworkService.getArtwork(30L)).thenReturn(artwork);
        AdminArtworkService service = new AdminArtworkService(artworks, artworkService, clock);

        assertThatThrownBy(() -> service.update(30L, new AdminArtworkUpdateRequest(null, null, null, SaleMode.SALE,
                ArtworkStatus.EXHIBITED))).hasMessage("artwork_price_required");
        assertThatThrownBy(() -> service.update(30L, new AdminArtworkUpdateRequest(null, null, null, null,
                ArtworkStatus.IN_AUCTION))).hasMessage("artwork_status_change_invalid [DRAFT, IN_AUCTION]");

        var updated = service.update(30L, new AdminArtworkUpdateRequest(new BigDecimal("350000"), "COP", 1,
                SaleMode.SALE, ArtworkStatus.EXHIBITED));

        assertThat(updated.status()).isEqualTo(ArtworkStatus.EXHIBITED);
        assertThat(artwork.getPublishedAt()).isEqualTo(NOW);
        verify(artworks).saveAndFlush(artwork);
    }
}
