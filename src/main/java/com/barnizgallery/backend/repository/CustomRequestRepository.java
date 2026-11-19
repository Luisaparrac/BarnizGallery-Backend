package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.CustomRequest;

/**
 * Spring Data repository for {@link CustomRequest}.
 */
public interface CustomRequestRepository extends JpaRepository<CustomRequest, Long> {
}
