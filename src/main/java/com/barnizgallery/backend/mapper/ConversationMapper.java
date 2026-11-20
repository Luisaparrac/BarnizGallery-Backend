package com.barnizgallery.backend.mapper;

import java.time.Instant;
import java.time.ZoneOffset;

import com.barnizgallery.backend.dto.ConversationResponse;
import com.barnizgallery.backend.dto.MessageResponse;
import com.barnizgallery.backend.entity.Conversation;
import com.barnizgallery.backend.entity.Message;

/**
 * Converts conversations and messages to their DTOs ({@code messages.created_at} is stored in UTC without zone).
 */
public final class ConversationMapper {

    static final int PREVIEW_LENGTH = 120;

    private ConversationMapper() {
    }

    public static ConversationResponse toResponse(Conversation c, Message last, long unread) {
        String preview = last == null ? "" : last.getContent();
        if (preview.length() > PREVIEW_LENGTH) {
            preview = preview.substring(0, PREVIEW_LENGTH);
        }
        return new ConversationResponse(c.getConversationId(), c.getCustomer().getUserId(), c.getCustomer().getName(),
                c.getMaster().getMasterId(), c.getMaster().getName(),
                c.getArtwork() == null ? null : c.getArtwork().getArtworkId(),
                c.getCustomRequest() == null ? null : c.getCustomRequest().getCustomRequestId(),
                c.getOrder() == null ? null : c.getOrder().getOrderId(), null, "OPEN",
                last == null ? c.getUpdatedAt() : createdAt(last), preview, unread);
    }

    public static MessageResponse toResponse(Message m) {
        return new MessageResponse(m.getMessageId(), m.getConversation().getConversationId(),
                m.getSender().getUserId(), m.getSender().getName(), m.getContent(), m.getReadAt(), createdAt(m));
    }

    private static Instant createdAt(Message m) {
        return m.getCreatedAt() == null ? null : m.getCreatedAt().toInstant(ZoneOffset.UTC);
    }
}
