package com.barnizgallery.backend.service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.request.IdentifyVisitorRequest;
import com.barnizgallery.backend.dto.request.TasteProfileRequest;
import com.barnizgallery.backend.dto.request.VisitorPreferencesRequest;
import com.barnizgallery.backend.dto.response.IdentifyVisitorResponse;
import com.barnizgallery.backend.dto.response.TasteProfileResponse;
import com.barnizgallery.backend.dto.response.VisitorResponse;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.VisitorMapper;
import com.barnizgallery.backend.model.entity.TasteProfile;
import com.barnizgallery.backend.model.entity.Visitor;
import com.barnizgallery.backend.repository.TasteProfileRepository;
import com.barnizgallery.backend.repository.VisitorRepository;

/**
 * Visitors are identified only by their email (no password).
 */
@Service
public class VisitorService {

    private final VisitorRepository visitorRepository;
    private final TasteProfileRepository tasteProfileRepository;

    public VisitorService(VisitorRepository visitorRepository, TasteProfileRepository tasteProfileRepository) {
        this.visitorRepository = visitorRepository;
        this.tasteProfileRepository = tasteProfileRepository;
    }

    /** Finds the visitor by email; if it does not exist, creates it. */
    @Transactional
    public IdentifyVisitorResponse identify(IdentifyVisitorRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        Optional<Visitor> existing = visitorRepository.findByEmailIgnoreCase(email);
        if (existing.isPresent()) {
            return new IdentifyVisitorResponse(VisitorMapper.toResponse(existing.get()), false);
        }
        Visitor created = visitorRepository.save(VisitorMapper.toNewVisitor(request, email));
        return new IdentifyVisitorResponse(VisitorMapper.toResponse(created), true);
    }

    @Transactional(readOnly = true)
    public VisitorResponse findById(Integer visitorId) {
        return VisitorMapper.toResponse(getVisitor(visitorId));
    }

    /** Changes the language and/or camera mode. Null fields are left as they are. */
    @Transactional
    public VisitorResponse updatePreferences(Integer visitorId, VisitorPreferencesRequest request) {
        Visitor visitor = getVisitor(visitorId);
        if (request.preferredLanguage() != null) {
            visitor.setPreferredLanguage(request.preferredLanguage());
        }
        if (request.cameraMode() != null) {
            visitor.setCameraMode(request.cameraMode());
        }
        return VisitorMapper.toResponse(visitor);
    }

    @Transactional(readOnly = true)
    public TasteProfileResponse getTasteProfile(Integer visitorId) {
        getVisitor(visitorId);
        return tasteProfileRepository.findByVisitorVisitorId(visitorId)
                .map(VisitorMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Taste profile of visitor " + visitorId
                        + " not found"));
    }

    /** Creates the taste profile or replaces its answers, and refreshes updated_at. */
    @Transactional
    public TasteProfileResponse saveTasteProfile(Integer visitorId, TasteProfileRequest request) {
        Visitor visitor = getVisitor(visitorId);
        TasteProfile profile = tasteProfileRepository.findByVisitorVisitorId(visitorId).orElseGet(() -> {
            TasteProfile created = new TasteProfile();
            created.setVisitor(visitor);
            return created;
        });
        VisitorMapper.apply(request, profile);
        profile.setUpdatedAt(LocalDateTime.now());
        return VisitorMapper.toResponse(tasteProfileRepository.save(profile));
    }

    /** Loads a visitor or throws 404. Used by other services too. */
    @Transactional(readOnly = true)
    public Visitor getVisitor(Integer visitorId) {
        return visitorRepository.findById(visitorId)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor", visitorId));
    }
}
