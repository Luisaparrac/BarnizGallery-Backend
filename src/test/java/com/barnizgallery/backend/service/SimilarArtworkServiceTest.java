package com.barnizgallery.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import com.barnizgallery.backend.TestEntities;
import com.barnizgallery.backend.dto.AiSimilarItem;
import com.barnizgallery.backend.dto.AiSimilarResponse;
import com.barnizgallery.backend.dto.SimilarArtworkResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Shape;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.exception.AiServiceException;
import com.barnizgallery.backend.patterns.adapter.AiServiceGateway;
import com.barnizgallery.backend.repository.ArtworkCatalogViewRepository;
import com.barnizgallery.backend.repository.ArtworkRepository;

class SimilarArtworkServiceTest {

    private final ArtworkService artworks = mock(ArtworkService.class);
    private final ArtworkRepository repository = mock(ArtworkRepository.class);
    private final ArtworkCatalogViewRepository catalog = mock(ArtworkCatalogViewRepository.class);
    private final AiServiceGateway ai = mock(AiServiceGateway.class);
    private final SimilarArtworkService service = new SimilarArtworkService(artworks, repository, catalog, ai);

    private final Store store = TestEntities.store(1, TestEntities.master(1));
    private final Artwork base = artwork(1, "plato", "rojo", "verde");
    private final Artwork twin = artwork(2, "plato", "rojo", "verde");
    private final Artwork cousin = artwork(3, "jarron", "rojo");
    private final Artwork stranger = artwork(4, "caja", "negro");

    @BeforeEach
    void gallery() {
        when(artworks.requirePublic(1L)).thenReturn(base);
        when(catalog.findAllArtworkIds()).thenReturn(List.of(1L, 2L, 3L, 4L));
        when(repository.findByDeletedAtIsNullAndStatusInOrderByArtworkIdAsc(any(), any()))
                .thenReturn(List.of(base, twin, cousin, stranger));
    }

    @AfterEach
    void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void withoutTheAiTheFixedRulesRankAndExplainInTheRequestLanguage() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        List<SimilarArtworkResponse> similar = service.similar(1L);

        assertThat(similar).extracting(SimilarArtworkResponse::artworkId).containsExactly(2L, 3L);
        assertThat(similar.get(0).source()).isEqualTo("rules");
        assertThat(similar.get(0).reason()).isEqualTo("Similar in: shape, colors (rojo, verde).");
    }

    @Test
    void aFailingAiFallsBackToTheRules() {
        when(ai.isAvailable()).thenReturn(true);
        when(ai.similar(any())).thenThrow(new AiServiceException(404, "invalid_request", "missing"));

        assertThat(service.similar(1L)).extracting(SimilarArtworkResponse::source).containsOnly("rules");
    }

    @Test
    void theAiRankingIsUsedWithItsSpanishExplanation() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));
        when(ai.isAvailable()).thenReturn(true);
        when(ai.similar(any())).thenReturn(new AiSimilarResponse(1L, "obra-1", List.of(
                new AiSimilarItem(3L, "obra-3", 1L, 0.7, List.of("rojo"), true, "Comparte el rojo."),
                new AiSimilarItem(99L, "obra-99", 1L, 0.9, List.of(), false, "No es candidata."))));

        List<SimilarArtworkResponse> similar = service.similar(1L);

        assertThat(similar).singleElement().satisfies(s -> {
            assertThat(s.artworkId()).isEqualTo(3L);
            assertThat(s.score()).isEqualTo(70.0);
            assertThat(s.reason()).isEqualTo("Comparte el rojo.");
            assertThat(s.source()).isEqualTo("ai");
        });
    }

    private Artwork artwork(long id, String shapeCode, String... colors) {
        Artwork artwork = TestEntities.artwork(id, store, ArtworkStatus.EXHIBITED, colors);
        Shape shape = new Shape();
        shape.setCode(shapeCode);
        artwork.setShape(shape);
        artwork.setMotifs(new LinkedHashSet<>());
        return artwork;
    }
}
