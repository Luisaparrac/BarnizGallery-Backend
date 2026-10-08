package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.model.entity.Master;

/**
 * Spring Data repository for {@link Master}.
 */
public interface MasterRepository extends JpaRepository<Master, Integer> {
}
