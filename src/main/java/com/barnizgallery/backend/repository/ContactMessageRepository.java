package com.barnizgallery.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.barnizgallery.backend.model.entity.ContactMessage;

/**
 * Spring Data repository for {@link ContactMessage}.
 */
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Integer> {

    /** Messages of a master, newest first, with the sender loaded in the same query. */
    @Query("select m from ContactMessage m join fetch m.visitor "
            + "where m.master.masterId = :masterId order by m.messageDate desc, m.messageId desc")
    List<ContactMessage> findByMasterWithVisitor(Integer masterId);
}
