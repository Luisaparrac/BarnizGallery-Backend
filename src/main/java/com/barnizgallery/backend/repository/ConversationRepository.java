package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.Conversation;

/**
 * Spring Data repository for {@link Conversation}.
 */
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
}
