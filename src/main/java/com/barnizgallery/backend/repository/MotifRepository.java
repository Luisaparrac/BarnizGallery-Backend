package com.barnizgallery.backend.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.Motif;

/**
 * Spring Data repository for {@link Motif}.
 */
public interface MotifRepository extends JpaRepository<Motif, Long> {

    List<Motif> findByCodeIn(Collection<String> codes);
}
