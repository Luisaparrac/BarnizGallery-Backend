package com.barnizgallery.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.Message;

/**
 * Spring Data repository for {@link Message}.
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationConversationIdOrderByMessageIdAsc(Long conversationId);

    Optional<Message> findFirstByConversationConversationIdOrderByMessageIdDesc(Long conversationId);

    long countByConversationConversationIdAndReadAtIsNullAndSenderUserIdNot(Long conversationId, Long readerId);
}
