package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.PendingVerificationView;

/**
 * Spring Data repository for {@link PendingVerificationView}.
 */
public interface PendingVerificationViewRepository extends JpaRepository<PendingVerificationView, String> {

    List<PendingVerificationView> findAllByOrderByMasterIdAscVerificationIdAsc();
}
