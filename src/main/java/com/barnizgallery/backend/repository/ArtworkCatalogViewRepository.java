package com.barnizgallery.backend.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.entity.ArtworkCatalogView;

/**
 * Spring Data repository for {@link ArtworkCatalogView}: the public gallery (exhibited or auctioned artworks of
 * published stores of approved masters).
 */
public interface ArtworkCatalogViewRepository extends JpaRepository<ArtworkCatalogView, Long> {

    @Query("select v.artworkId from ArtworkCatalogView v")
    List<Long> findAllArtworkIds();

    /**
     * Gallery page filtered by color, motif, shape and style codes, currency, price range, store slug and a text
     * search, sorted by {@code recent} (default), {@code priceAsc}, {@code priceDesc} or {@code popular}.
     */
    @Query(nativeQuery = true, value = """
            SELECT v.* FROM museo.v_artwork_catalog v
            JOIN museo.artworks a ON a.artwork_id = v.artwork_id
            LEFT JOIN museo.shapes sh ON sh.shape_id = a.shape_id
            LEFT JOIN museo.styles st ON st.style_id = a.style_id
            WHERE (CAST(:color AS text) IS NULL OR v.color_codes @> jsonb_build_array(CAST(:color AS text)))
              AND (CAST(:motif AS text) IS NULL OR v.motif_codes @> jsonb_build_array(CAST(:motif AS text)))
              AND (CAST(:shape AS text) IS NULL OR sh.code = CAST(:shape AS text))
              AND (CAST(:style AS text) IS NULL OR st.code = CAST(:style AS text))
              AND (CAST(:currency AS text) IS NULL OR v.currency = CAST(:currency AS text))
              AND (CAST(:minPrice AS numeric) IS NULL OR v.price >= CAST(:minPrice AS numeric))
              AND (CAST(:maxPrice AS numeric) IS NULL OR v.price <= CAST(:maxPrice AS numeric))
              AND (CAST(:store AS text) IS NULL OR v.store_slug = CAST(:store AS text))
              AND (CAST(:search AS text) IS NULL
                   OR v.title_es ILIKE '%' || CAST(:search AS text) || '%'
                   OR v.title_en ILIKE '%' || CAST(:search AS text) || '%'
                   OR v.master_name ILIKE '%' || CAST(:search AS text) || '%')
            ORDER BY
              CASE WHEN CAST(:sort AS text) = 'priceAsc' THEN v.price END ASC NULLS LAST,
              CASE WHEN CAST(:sort AS text) = 'priceDesc' THEN v.price END DESC NULLS LAST,
              CASE WHEN CAST(:sort AS text) = 'popular'
                   THEN (SELECT count(*) FROM museo.favorites f WHERE f.artwork_id = v.artwork_id) END DESC NULLS LAST,
              v.artwork_id DESC
            LIMIT :limit OFFSET :offset
            """)
    List<ArtworkCatalogView> search(String color, String motif, String shape, String style, String currency,
            BigDecimal minPrice, BigDecimal maxPrice, String store, String search, String sort, int limit, int offset);
}
