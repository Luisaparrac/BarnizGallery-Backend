package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.MasterVerification;

/**
 * Spring Data repository for {@link MasterVerification}.
 */
public interface MasterVerificationRepository extends JpaRepository<MasterVerification, Long> {
}
