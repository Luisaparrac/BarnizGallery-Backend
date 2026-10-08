package com.barnizgallery.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.request.ContactMessageRequest;
import com.barnizgallery.backend.dto.response.ContactMessageResponse;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.mapper.ContactMessageMapper;
import com.barnizgallery.backend.model.entity.Artwork;
import com.barnizgallery.backend.model.entity.ContactMessage;
import com.barnizgallery.backend.model.entity.Master;
import com.barnizgallery.backend.repository.ContactMessageRepository;

/**
 * Messages from visitors to masters.
 */
@Service
public class ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;
    private final VisitorService visitorService;
    private final MasterService masterService;
    private final ArtworkService artworkService;

    public ContactMessageService(ContactMessageRepository contactMessageRepository, VisitorService visitorService,
            MasterService masterService, ArtworkService artworkService) {
        this.contactMessageRepository = contactMessageRepository;
        this.visitorService = visitorService;
        this.masterService = masterService;
        this.artworkService = artworkService;
    }

    /** Saves a message. If it is about an artwork, the artwork must be in the master's room. */
    @Transactional
    public ContactMessageResponse send(ContactMessageRequest request) {
        ContactMessage message = new ContactMessage();
        message.setVisitor(visitorService.getVisitor(request.visitorId()));
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
    public List<ContactMessageResponse> findByMaster(Integer masterId) {
        masterService.getMaster(masterId);
        return contactMessageRepository.findByMasterWithVisitor(masterId).stream()
                .map(ContactMessageMapper::toResponse)
                .toList();
    }
}
