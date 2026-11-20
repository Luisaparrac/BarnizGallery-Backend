package com.barnizgallery.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.Master;

/**
 * Spring Data repository for {@link Master}.
 */
public interface MasterRepository extends JpaRepository<Master, Long> {

    /** The master profile linked to a user account, if any. */
    Optional<Master> findByUserUserId(Long userId);
}
