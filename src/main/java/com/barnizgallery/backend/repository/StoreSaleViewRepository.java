package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.StoreSaleView;

/**
 * Spring Data repository for {@link StoreSaleView}.
 */
public interface StoreSaleViewRepository extends JpaRepository<StoreSaleView, Long> {
}
