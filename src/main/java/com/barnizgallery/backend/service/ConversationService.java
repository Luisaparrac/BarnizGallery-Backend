package com.barnizgallery.backend.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.ConversationResponse;
import com.barnizgallery.backend.dto.StartConversationRequest;
import com.barnizgallery.backend.entity.Conversation;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.entity.Message;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.ConversationMapper;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.ConversationRepository;
import com.barnizgallery.backend.repository.CustomRequestRepository;
import com.barnizgallery.backend.repository.MasterRepository;
import com.barnizgallery.backend.repository.MessageRepository;
import com.barnizgallery.backend.repository.OrderRepository;
import com.barnizgallery.backend.repository.UserRepository;
import com.barnizgallery.backend.security.CurrentUser;

/**
 * Conversations between a customer and a master. A user only sees the conversations where they are the customer
 * or the master's account (others are 404). The database checks that the context (artwork, custom request or
 * order) belongs to both and that every sender takes part.
 */
@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MasterRepository masterRepository;
    private final UserRepository userRepository;
    private final ArtworkRepository artworkRepository;
    private final CustomRequestRepository customRequestRepository;
    private final OrderRepository orderRepository;
    private final CurrentUser currentUser;
    private final Clock clock;

    public ConversationService(ConversationRepository conversationRepository, MessageRepository messageRepository,
            MasterRepository masterRepository, UserRepository userRepository, ArtworkRepository artworkRepository,
            CustomRequestRepository customRequestRepository, OrderRepository orderRepository, CurrentUser currentUser,
            Clock clock) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.masterRepository = masterRepository;
        this.userRepository = userRepository;
        this.artworkRepository = artworkRepository;
        this.customRequestRepository = customRequestRepository;
        this.orderRepository = orderRepository;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> mine() {
        Long userId = currentUser.requireId();
        return conversationRepository.findOfParticipant(userId).stream().map(c -> toResponse(c, userId)).toList();
    }

    /** Creates the conversation with its first message (the logged-in user is the customer side). */
    @Transactional
    public ConversationResponse start(StartConversationRequest request) {
        long contexts = Stream.of(request.artworkId(), request.customRequestId(), request.orderId())
                .filter(id -> id != null).count();
        if (contexts > 1) {
            throw BusinessRuleException.unprocessable(ErrorCode.CONVERSATION_CONTEXT_INVALID);
        }
        Master master = masterRepository.findById(request.masterId())
                .filter(Master::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MASTER_NOT_FOUND, request.masterId()));
        Long userId = currentUser.requireId();
        if (master.getUser() != null && master.getUser().getUserId().equals(userId)) {
            throw BusinessRuleException.unprocessable(ErrorCode.CONVERSATION_CONTEXT_INVALID);
        }
        User customer = userRepository.getReferenceById(userId);
        Conversation conversation = new Conversation();
        conversation.setCustomer(customer);
        conversation.setMaster(master);
        if (request.artworkId() != null) {
            conversation.setArtwork(artworkRepository.getReferenceById(request.artworkId()));
        }
        if (request.customRequestId() != null) {
            conversation.setCustomRequest(customRequestRepository.getReferenceById(request.customRequestId()));
        }
        if (request.orderId() != null) {
            conversation.setOrder(orderRepository.getReferenceById(request.orderId()));
        }
        conversationRepository.saveAndFlush(conversation);
        addMessage(conversation, customer, request.content());
        return toResponse(conversationRepository.findById(conversation.getConversationId()).orElseThrow(), userId);
    }

    /** A conversation where the logged-in user takes part (404 otherwise). */
    @Transactional(readOnly = true)
    public Conversation mine(Long conversationId) {
        Long userId = currentUser.requireId();
        return conversationRepository.findById(conversationId)
                .filter(c -> c.getCustomer().getUserId().equals(userId)
                        || (c.getMaster().getUser() != null && c.getMaster().getUser().getUserId().equals(userId)))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CONVERSATION_NOT_FOUND, conversationId));
    }

    Message addMessage(Conversation conversation, User sender, String content) {
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(content.strip());
        message.setCreatedAt(LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC));
        conversation.setUpdatedAt(clock.instant());
        return messageRepository.save(message);
    }

    private ConversationResponse toResponse(Conversation c, Long readerId) {
        Message last = messageRepository.findFirstByConversationConversationIdOrderByMessageIdDesc(c.getConversationId())
                .orElse(null);
        long unread = messageRepository.countByConversationConversationIdAndReadAtIsNullAndSenderUserIdNot(
                c.getConversationId(), readerId);
        return ConversationMapper.toResponse(c, last, unread);
    }
}
