package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.CustomRequestDesign;

/**
 * Spring Data repository for {@link CustomRequestDesign}.
 */
public interface CustomRequestDesignRepository extends JpaRepository<CustomRequestDesign, Long> {

    List<CustomRequestDesign> findByCustomRequestCustomRequestIdOrderByDesignIdAsc(Long customRequestId);

    long countByCustomRequestCustomRequestId(Long customRequestId);
}
