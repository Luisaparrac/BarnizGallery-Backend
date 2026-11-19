package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.Message;

/**
 * Spring Data repository for {@link Message}.
 */
public interface MessageRepository extends JpaRepository<Message, Long> {
}
