package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.ArtworkFilter;
import com.barnizgallery.backend.dto.ArtworkRequest;
import com.barnizgallery.backend.dto.ArtworkSummaryResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.SaleMode;
import com.barnizgallery.backend.repository.ArtworkCatalogViewRepository;
import com.barnizgallery.backend.repository.ArtworkRepository;

/** Artworks of the artisan's own store: ownership, status changes, sale rules and soft delete. */
class ArtisanArtworkServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-20T12:00:00Z");

    private final ArtworkRepository artworks = mock(ArtworkRepository.class);
    private final StoreEditorService editor = mock(StoreEditorService.class);
    private final CatalogCodeResolver catalogs = mock(CatalogCodeResolver.class);
    private final ArtisanArtworkService service = new ArtisanArtworkService(artworks, editor, catalogs,
            Clock.fixed(NOW, ZoneOffset.UTC), mock(ArtworkService.class));

    private final Store store = TestEntities.store(5, TestEntities.master(70));
    private final Artwork own = TestEntities.artwork(10, store, ArtworkStatus.DRAFT);

    @BeforeEach
    void artisanWithStore() {
        when(editor.myStore()).thenReturn(store);
        when(catalogs.colors(any())).thenReturn(new LinkedHashSet<>());
        when(catalogs.motifs(any())).thenReturn(new LinkedHashSet<>());
        when(artworks.findById(10L)).thenReturn(Optional.of(own));
        when(artworks.findById(11L)).thenReturn(Optional.of(
                TestEntities.artwork(11, TestEntities.store(6, TestEntities.master(71)), ArtworkStatus.EXHIBITED)));
    }

    @Test
    void newArtworksAreDraftsOfTheOwnStoreAndPricesNeedACurrency() {
        when(artworks.save(any(Artwork.class))).thenAnswer(inv -> inv.getArgument(0));

        ArtworkSummaryResponse created = service.create(request(SaleMode.SALE, "120", "USD", 2));

        assertThat(created.status()).isEqualTo(ArtworkStatus.DRAFT);
        assertThat(created.storeId()).isEqualTo(5L);
        assertThatThrownBy(() -> service.create(request(SaleMode.SALE, "120", null, 2)))
                .hasMessage("artwork_currency_required");
    }

    @Test
    void artworksOfOtherStoresOrDeletedOnesAreNotFound() {
        assertThatThrownBy(() -> service.update(11L, request(null, null, null, null)))
                .hasMessage("artwork_not_found [11]");
        own.setDeletedAt(NOW);
        assertThatThrownBy(() -> service.changeStatus(10L, ArtworkStatus.EXHIBITED))
                .hasMessage("artwork_not_found [10]");
    }

    @Test
    void exhibitingNeedsSaleModeAndForSaleAPriceAndStock() {
        assertThatThrownBy(() -> service.changeStatus(10L, ArtworkStatus.EXHIBITED))
                .hasMessage("artwork_sale_mode_required");
        own.setSaleMode(SaleMode.SALE);
        assertThatThrownBy(() -> service.changeStatus(10L, ArtworkStatus.EXHIBITED))
                .hasMessage("artwork_price_required");
        own.setPrice(new BigDecimal("80"));
        own.setCurrency("COP");
        assertThatThrownBy(() -> service.changeStatus(10L, ArtworkStatus.EXHIBITED))
                .hasMessage("artwork_stock_required");
        own.setStock(1);

        assertThat(service.changeStatus(10L, ArtworkStatus.EXHIBITED).status()).isEqualTo(ArtworkStatus.EXHIBITED);
        assertThat(own.getPublishedAt()).isEqualTo(NOW);
    }

    @Test
    void onlyTheAllowedStatusChangesAreAccepted() {
        own.setStatus(ArtworkStatus.IN_AUCTION);
        assertThatThrownBy(() -> service.changeStatus(10L, ArtworkStatus.DRAFT))
                .hasMessage("artwork_status_change_invalid [IN_AUCTION, DRAFT]");
        own.setStatus(ArtworkStatus.SOLD);
        assertThatThrownBy(() -> service.changeStatus(10L, ArtworkStatus.EXHIBITED))
                .hasMessage("artwork_status_change_invalid [SOLD, EXHIBITED]");
        assertThat(service.changeStatus(10L, ArtworkStatus.ARCHIVED).status()).isEqualTo(ArtworkStatus.ARCHIVED);
    }

    @Test
    void softDeleteKeepsTheRowAndRefusesArtworksInUse() {
        own.setStatus(ArtworkStatus.RESERVED);
        assertThatThrownBy(() -> service.delete(10L)).hasMessage("artwork_in_use [RESERVED]");
        own.setStatus(ArtworkStatus.EXHIBITED);

        service.delete(10L);

        assertThat(own.getDeletedAt()).isEqualTo(NOW);
        verify(artworks, never()).delete(any());
    }

    @Test
    void galleryPriceRangeNeedsACurrency() {
        ArtworkCatalogViewRepository catalog = mock(ArtworkCatalogViewRepository.class);
        GalleryService gallery = new GalleryService(mock(GalleryTreeBuilder.class), catalog, artworks,
                mock(com.barnizgallery.backend.repository.PhotoRepository.class),
                mock(com.barnizgallery.backend.repository.FavoriteRepository.class));

        assertThatThrownBy(() -> gallery.artworks(new ArtworkFilter(null, null, null, null, null,
                BigDecimal.ONE, null, null, null, null))).hasMessage("price_filter_currency_required");
        gallery.artworks(new ArtworkFilter("rojo", null, null, null, "USD", BigDecimal.ONE, null, null, 2, null));

        verify(catalog).search("rojo", null, null, null, "USD", BigDecimal.ONE, null, null,
                GalleryService.DEFAULT_PAGE_SIZE, 2 * GalleryService.DEFAULT_PAGE_SIZE);
    }

    private static ArtworkRequest request(SaleMode mode, String price, String currency, Integer stock) {
        return new ArtworkRequest("Bandeja", "Tray", null, null, null, null, null, null, null, mode,
                price == null ? null : new BigDecimal(price), currency, stock, List.of(), List.of(), null, null);
    }
}
