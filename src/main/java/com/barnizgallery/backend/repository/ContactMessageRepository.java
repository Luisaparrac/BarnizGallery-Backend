package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.model.entity.ContactMessage;

/**
 * Spring Data repository for {@link ContactMessage}.
 */
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Integer> {
}
