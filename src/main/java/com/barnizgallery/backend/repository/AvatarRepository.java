package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.Avatar;

/**
 * Spring Data repository for {@link Avatar}.
 */
public interface AvatarRepository extends JpaRepository<Avatar, Long> {
}
