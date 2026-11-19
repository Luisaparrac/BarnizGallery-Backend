package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.StoreTemplate;

/**
 * Spring Data repository for {@link StoreTemplate}.
 */
public interface StoreTemplateRepository extends JpaRepository<StoreTemplate, Long> {
}
