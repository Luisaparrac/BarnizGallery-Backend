package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.ConversationResponse;
import com.barnizgallery.backend.dto.MessageResponse;
import com.barnizgallery.backend.dto.SendMessageRequest;
import com.barnizgallery.backend.dto.StartConversationRequest;
import com.barnizgallery.backend.service.ConversationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Conversations of the logged-in user with masters.
 */
@RestController
@RequestMapping("/api/conversations")
@Tag(name = "Conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    @Operation(summary = "Conversations where the user takes part, last updated first")
    public List<ConversationResponse> mine() {
        return conversationService.mine();
    }

    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "Messages of a conversation where the user takes part, oldest first (404 otherwise)")
    public List<MessageResponse> messages(@PathVariable Long conversationId) {
        return conversationService.messages(conversationId);
    }

    @PostMapping("/{conversationId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Send a message; the other participant is notified")
    public MessageResponse send(@PathVariable Long conversationId, @Valid @RequestBody SendMessageRequest request) {
        return conversationService.send(conversationId, request.content());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Start a conversation with a master and send its first message")
    public ConversationResponse start(@Valid @RequestBody StartConversationRequest request) {
        return conversationService.start(request);
    }
}
