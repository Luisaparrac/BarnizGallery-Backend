package com.barnizgallery.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.StoreTemplate;

/**
 * Spring Data repository for {@link StoreTemplate}.
 */
public interface StoreTemplateRepository extends JpaRepository<StoreTemplate, Long> {

    List<StoreTemplate> findByActiveTrueOrderByCodeAsc();

    List<StoreTemplate> findAllByOrderByCodeAsc();

    Optional<StoreTemplate> findByCode(String code);
}
