package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.model.entity.Photo;

/**
 * Spring Data repository for {@link Photo}.
 */
public interface PhotoRepository extends JpaRepository<Photo, Integer> {
}
