package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.model.entity.Photo;

/**
 * Spring Data repository for {@link Photo}.
 */
public interface PhotoRepository extends JpaRepository<Photo, Integer> {

    List<Photo> findByArtworkArtworkIdOrderByPhotoIdAsc(Integer artworkId);
}
