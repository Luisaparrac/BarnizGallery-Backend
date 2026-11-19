package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.Motif;

/**
 * Spring Data repository for {@link Motif}.
 */
public interface MotifRepository extends JpaRepository<Motif, Long> {
}
