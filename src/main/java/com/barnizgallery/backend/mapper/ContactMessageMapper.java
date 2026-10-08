package com.barnizgallery.backend.mapper;

import com.barnizgallery.backend.dto.ContactMessageResponse;
import com.barnizgallery.backend.entity.ContactMessage;

/**
 * Converts {@link ContactMessage} to its DTO.
 */
public final class ContactMessageMapper {

    private ContactMessageMapper() {
    }

    public static ContactMessageResponse toResponse(ContactMessage message) {
        Integer artworkId = message.getArtwork() == null ? null : message.getArtwork().getArtworkId();
        return new ContactMessageResponse(message.getMessageId(), message.getVisitor().getVisitorId(),
                message.getVisitor().getName(), message.getVisitor().getEmail(), message.getMaster().getMasterId(),
                artworkId, message.getContent(), message.getMessageDate());
    }
}
