package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.entity.Conversation;

/**
 * Spring Data repository for {@link Conversation}.
 */
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /** Conversations where the user is the customer or the master's account, last updated first. */
    @Query("select c from Conversation c join fetch c.customer join fetch c.master m left join m.user mu "
            + "where c.customer.userId = :userId or mu.userId = :userId order by c.updatedAt desc, c.conversationId desc")
    List<Conversation> findOfParticipant(Long userId);
}
