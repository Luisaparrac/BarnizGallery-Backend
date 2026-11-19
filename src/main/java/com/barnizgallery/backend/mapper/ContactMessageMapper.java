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
        Long artworkId = message.getArtwork() == null ? null : message.getArtwork().getArtworkId();
        return new ContactMessageResponse(message.getMessageId(), message.getUser().getUserId(),
                message.getUser().getName(), message.getUser().getEmail(), message.getMaster().getMasterId(),
                artworkId, message.getContent(), message.getMessageDate());
    }
}
