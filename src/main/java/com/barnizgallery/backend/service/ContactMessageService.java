package com.barnizgallery.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.ContactMessageRequest;
import com.barnizgallery.backend.dto.ContactMessageResponse;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.ContactMessage;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.mapper.ContactMessageMapper;
import com.barnizgallery.backend.repository.ContactMessageRepository;

/**
 * Messages from users to masters.
 */
@Service
public class ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;
    private final UserService userService;
    private final MasterService masterService;
    private final ArtworkService artworkService;

    public ContactMessageService(ContactMessageRepository contactMessageRepository, UserService userService,
            MasterService masterService, ArtworkService artworkService) {
        this.contactMessageRepository = contactMessageRepository;
        this.userService = userService;
        this.masterService = masterService;
        this.artworkService = artworkService;
    }

    /** Saves a message. If it is about an artwork, the artwork must be in the master's room. */
    @Transactional
    public ContactMessageResponse send(ContactMessageRequest request) {
        ContactMessage message = new ContactMessage();
        message.setUser(userService.getUser(request.userId()));
        Master master = masterService.getMaster(request.masterId());
        message.setMaster(master);
        if (request.artworkId() != null) {
            Artwork artwork = artworkService.getArtwork(request.artworkId());
            if (!artwork.getRoom().getMaster().getMasterId().equals(master.getMasterId())) {
                throw BusinessRuleException.unprocessable("Artwork " + artwork.getArtworkId()
                        + " does not belong to the room of master " + master.getMasterId());
            }
            message.setArtwork(artwork);
        }
        message.setContent(request.content().trim());
        return ContactMessageMapper.toResponse(contactMessageRepository.save(message));
    }

    @Transactional(readOnly = true)
    public List<ContactMessageResponse> findByMaster(Long masterId) {
        masterService.getMaster(masterId);
        return contactMessageRepository.findByMasterWithUser(masterId).stream()
                .map(ContactMessageMapper::toResponse)
                .toList();
    }
}
