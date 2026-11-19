package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.PendingVerificationView;

/**
 * Spring Data repository for {@link PendingVerificationView}.
 */
public interface PendingVerificationViewRepository extends JpaRepository<PendingVerificationView, String> {
}
