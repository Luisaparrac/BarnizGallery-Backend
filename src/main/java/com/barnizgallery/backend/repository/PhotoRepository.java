package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.Photo;

/**
 * Spring Data repository for {@link Photo}.
 */
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    List<Photo> findByArtworkArtworkIdOrderBySortOrderAscPhotoIdAsc(Long artworkId);

    /** Every photo, primary photos first and then by order, so the first one of each artwork is its thumbnail. */
    List<Photo> findAllByOrderByPrimaryDescSortOrderAscPhotoIdAsc();
}
