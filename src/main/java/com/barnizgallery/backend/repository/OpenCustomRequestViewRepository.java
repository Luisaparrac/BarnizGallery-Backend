package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.OpenCustomRequestView;

/**
 * Spring Data repository for {@link OpenCustomRequestView}.
 */
public interface OpenCustomRequestViewRepository extends JpaRepository<OpenCustomRequestView, Long> {
}
