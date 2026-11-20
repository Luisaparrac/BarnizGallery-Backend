package com.barnizgallery.backend.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.barnizgallery.backend.entity.CatalogEntry;

/**
 * Queries shared by the simple catalogs (colors, shapes, motifs, styles and avatars).
 *
 * @param <E> the catalog entity
 */
@NoRepositoryBean
public interface CatalogEntryRepository<E extends CatalogEntry> extends JpaRepository<E, Long> {

    Optional<E> findByCode(String code);

    List<E> findByCodeIn(Collection<String> codes);

    /** Every entry, active or not, in list order. */
    List<E> findAllByOrderBySortOrderAscCodeAsc();

    /** The active entries, in list order. */
    List<E> findByActiveTrueOrderBySortOrderAscCodeAsc();
}
