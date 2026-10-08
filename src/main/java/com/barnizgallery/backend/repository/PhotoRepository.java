package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.entity.Photo;

/**
 * Spring Data repository for {@link Photo}.
 */
public interface PhotoRepository extends JpaRepository<Photo, Integer> {

    List<Photo> findByArtworkArtworkIdOrderByPhotoIdAsc(Integer artworkId);

    /** The first photo (lowest id) of every artwork, in a single query. Used as thumbnail. */
    @Query("select p from Photo p where p.photoId in "
            + "(select min(p2.photoId) from Photo p2 group by p2.artwork.artworkId)")
    List<Photo> findFirstPhotoOfEachArtwork();
}
