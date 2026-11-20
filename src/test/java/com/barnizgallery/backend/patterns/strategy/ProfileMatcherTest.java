package com.barnizgallery.backend.patterns.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.TestFactories;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.BudgetRange;
import com.barnizgallery.backend.entity.Motif;
import com.barnizgallery.backend.entity.Shape;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.entity.Style;
import com.barnizgallery.backend.entity.TasteProfile;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.InteractionAction;
import com.barnizgallery.backend.enums.Language;
import com.barnizgallery.backend.repository.ArtworkCatalogViewRepository;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.InteractionRepository;
import com.barnizgallery.backend.repository.StoreInteraction;
import com.barnizgallery.backend.repository.TasteProfileRepository;

class ProfileMatcherTest {

    private final User user = TestEntities.user(1, Language.ES);
    private final Store store = TestEntities.store(1, TestEntities.master(1));

    @Test
    void everyDeclaredPartOfTheProfileCounts() {
        TasteProfile profile = profile();
        Artwork artwork = artwork(10, "rojo");
        artwork.setShape(shape("bandeja"));
        artwork.setStyle(style("tradicional"));
        artwork.getMotifs().add(motif("floral"));
        artwork.setPrice(new BigDecimal("80"));
        artwork.setCurrency("USD");

        ProfileMatch match = ProfileMatcher.match(profile, artwork);

        // colors 35 x 1/2 + shape 20 + style 20 + motifs 15 + budget 10 = 82.5 of 100
        assertThat(match.score()).isEqualTo(82.5);
        assertThat(ProfileMatcher.reason(user, match)).isEqualTo(
                "Coincide con: colores rojo; tu forma preferida; tu estilo preferido; figuras floral; tu presupuesto");
    }

    @Test
    void budgetNeedsTheSameCurrencyAndAPriceInsideTheRange() {
        TasteProfile profile = profile();
        Artwork expensive = artwork(11);
        expensive.setPrice(new BigDecimal("150"));
        expensive.setCurrency("USD");
        Artwork otherCurrency = artwork(12);
        otherCurrency.setPrice(new BigDecimal("80"));
        otherCurrency.setCurrency("COP");

        assertThat(ProfileMatcher.match(profile, expensive).budget()).isFalse();
        assertThat(ProfileMatcher.match(profile, otherCurrency).budget()).isFalse();
        assertThat(ProfileMatcher.match(profile, artwork(13)).matchesSomething()).isFalse();
    }

    @Test
    void profileStrategyRecommendsArtworksBestFirst() {
        TasteProfileRepository profiles = mock(TasteProfileRepository.class);
        ArtworkRepository artworks = mock(ArtworkRepository.class);
        when(profiles.findByUserUserId(1L)).thenReturn(Optional.of(profile()));
        when(artworks.findByDeletedAtIsNullAndStatusInOrderByArtworkIdAsc(anyCollection(), any()))
                .thenReturn(List.of(artwork(10, "rojo"), artwork(11, "rojo", "verde"), artwork(12, "azul")));

        List<RecommendationResult> results = new ProfileBasedStrategy(profiles, catalog(artworks, 10L, 11L, 12L))
                .recommendArtworks(user);

        assertThat(results).extracting(RecommendationResult::artworkId).containsExactly(11L, 10L);
        assertThat(results).allSatisfy(r -> assertThat(r.storeId()).isNull());
    }

    @Test
    void interactionStrategyRecommendsTheArtworksOfInterestingStores() {
        InteractionRepository interactions = mock(InteractionRepository.class);
        ArtworkRepository artworks = mock(ArtworkRepository.class);
        StoreInteraction visit = mock(StoreInteraction.class);
        when(visit.getStoreId()).thenReturn(1L);
        when(visit.getAction()).thenReturn(InteractionAction.STORE_VISIT);
        when(interactions.findStoreInteractions(1L)).thenReturn(List.of(visit));
        Artwork elsewhere = TestEntities.artwork(20, TestEntities.store(2, TestEntities.master(2)),
                ArtworkStatus.EXHIBITED);
        when(artworks.findByDeletedAtIsNullAndStatusInOrderByArtworkIdAsc(anyCollection(), any()))
                .thenReturn(List.of(artwork(10), elsewhere));

        List<RecommendationResult> results = new InteractionBasedStrategy(interactions, TestFactories.provider(),
                catalog(artworks, 10L, 20L)).recommendArtworks(user);

        assertThat(results).singleElement().satisfies(r -> {
            assertThat(r.artworkId()).isEqualTo(10L);
            assertThat(r.score()).isEqualByComparingTo("100");
            assertThat(r.reason()).isEqualTo("Interactuaste 1 veces con esta tienda o sus obras");
        });
    }

    private static PublicArtworkCatalog catalog(ArtworkRepository artworks, Long... publicIds) {
        ArtworkCatalogViewRepository view = mock(ArtworkCatalogViewRepository.class);
        when(view.findAllArtworkIds()).thenReturn(List.of(publicIds));
        return new PublicArtworkCatalog(artworks, view);
    }

    private TasteProfile profile() {
        TasteProfile profile = new TasteProfile();
        profile.getColors().add(TestEntities.color("rojo"));
        profile.getColors().add(TestEntities.color("verde"));
        profile.getShapes().add(shape("bandeja"));
        profile.getStyles().add(style("tradicional"));
        profile.getMotifs().add(motif("floral"));
        BudgetRange budget = new BudgetRange();
        budget.setCode("0-100");
        budget.setMinAmount(BigDecimal.ZERO);
        budget.setMaxAmount(new BigDecimal("100"));
        budget.setCurrency("USD");
        profile.setBudgetRange(budget);
        return profile;
    }

    private Artwork artwork(long id, String... colors) {
        return TestEntities.artwork(id, store, ArtworkStatus.EXHIBITED, colors);
    }

    private static Shape shape(String code) {
        Shape shape = new Shape();
        shape.setCode(code);
        return shape;
    }

    private static Style style(String code) {
        Style style = new Style();
        style.setCode(code);
        return style;
    }

    private static Motif motif(String code) {
        Motif motif = new Motif();
        motif.setCode(code);
        return motif;
    }
}
