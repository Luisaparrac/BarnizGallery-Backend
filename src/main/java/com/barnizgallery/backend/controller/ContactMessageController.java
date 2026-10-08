package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.ContactMessageRequest;
import com.barnizgallery.backend.dto.ContactMessageResponse;
import com.barnizgallery.backend.service.ContactMessageService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Messages from visitors to masters.
 */
@RestController
@Tag(name = "Contact")
public class ContactMessageController {

    private final ContactMessageService contactMessageService;

    public ContactMessageController(ContactMessageService contactMessageService) {
        this.contactMessageService = contactMessageService;
    }

    @PostMapping("/api/contact-messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ContactMessageResponse send(@Valid @RequestBody ContactMessageRequest request) {
        return contactMessageService.send(request);
    }

    @GetMapping("/api/masters/{id}/messages")
    public List<ContactMessageResponse> findByMaster(@PathVariable Integer id) {
        return contactMessageService.findByMaster(id);
    }
}
