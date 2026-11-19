package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.AuthToken;

/**
 * Spring Data repository for {@link AuthToken}.
 */
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {
}
