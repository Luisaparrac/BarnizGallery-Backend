package com.barnizgallery.backend.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.CustomRequestInput;
import com.barnizgallery.backend.dto.CustomRequestResponse;
import com.barnizgallery.backend.entity.CustomOffer;
import com.barnizgallery.backend.entity.CustomRequest;
import com.barnizgallery.backend.enums.CustomRequestStatus;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.OfferStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.CustomRequestMapper;
import com.barnizgallery.backend.repository.ColorRepository;
import com.barnizgallery.backend.repository.CustomOfferRepository;
import com.barnizgallery.backend.repository.CustomRequestDesignRepository;
import com.barnizgallery.backend.repository.CustomRequestRepository;
import com.barnizgallery.backend.repository.MasterRepository;
import com.barnizgallery.backend.repository.MotifRepository;
import com.barnizgallery.backend.repository.ShapeRepository;
import com.barnizgallery.backend.repository.StyleRepository;
import com.barnizgallery.backend.repository.UserRepository;
import com.barnizgallery.backend.security.CurrentUser;

/**
 * Custom requests of the logged-in customer: create and edit drafts, publish (a description is required, a design is
 * not), cancel (draft or open; active offers are rejected) and read. A request of someone else is 404.
 */
@Service
public class CustomRequestService {

    private final CustomRequestRepository requestRepository;
    private final CustomOfferRepository offerRepository;
    private final CustomRequestDesignRepository designRepository;
    private final UserRepository userRepository;
    private final MasterRepository masterRepository;
    private final ShapeRepository shapeRepository;
    private final StyleRepository styleRepository;
    private final ColorRepository colorRepository;
    private final MotifRepository motifRepository;
    private final AppSettingsService settings;
    private final CurrentUser currentUser;

    public CustomRequestService(CustomRequestRepository requestRepository, CustomOfferRepository offerRepository,
            CustomRequestDesignRepository designRepository, UserRepository userRepository,
            MasterRepository masterRepository, ShapeRepository shapeRepository, StyleRepository styleRepository,
            ColorRepository colorRepository, MotifRepository motifRepository, AppSettingsService settings,
            CurrentUser currentUser) {
        this.requestRepository = requestRepository;
        this.offerRepository = offerRepository;
        this.designRepository = designRepository;
        this.userRepository = userRepository;
        this.masterRepository = masterRepository;
        this.shapeRepository = shapeRepository;
        this.styleRepository = styleRepository;
        this.colorRepository = colorRepository;
        this.motifRepository = motifRepository;
        this.settings = settings;
        this.currentUser = currentUser;
    }

    @Transactional
    public CustomRequestResponse create(CustomRequestInput input) {
        CustomRequest request = new CustomRequest();
        request.setBuyer(userRepository.getReferenceById(currentUser.requireId()));
        request.setCurrency(settings.text(AppSettingsService.CURRENCY_DEFAULT));
        request.setStatus(CustomRequestStatus.DRAFT);
        apply(input, request);
        return toResponse(requestRepository.save(request));
    }

    /** Edits a draft (409 once it is published). */
    @Transactional
    public CustomRequestResponse update(Long id, CustomRequestInput input) {
        CustomRequest request = myRequest(id);
        requireStatus(request, CustomRequestStatus.DRAFT);
        apply(input, request);
        return toResponse(request);
    }

    /** Draft → open. The description must not be blank; a design is not required. */
    @Transactional
    public CustomRequestResponse publish(Long id) {
        CustomRequest request = myRequest(id);
        requireStatus(request, CustomRequestStatus.DRAFT);
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw BusinessRuleException.unprocessable(ErrorCode.CUSTOM_REQUEST_DESCRIPTION_REQUIRED);
        }
        request.setStatus(CustomRequestStatus.OPEN);
        return toResponse(request);
    }

    /** Draft or open → cancelled; the active offers are rejected. After the deposit there is no cancellation. */
    @Transactional
    public CustomRequestResponse cancel(Long id) {
        CustomRequest request = myRequest(id);
        requireStatus(request, CustomRequestStatus.DRAFT, CustomRequestStatus.OPEN);
        offers(request).stream().filter(o -> o.getStatus() == OfferStatus.ACTIVE)
                .forEach(o -> o.setStatus(OfferStatus.REJECTED));
        request.setStatus(CustomRequestStatus.CANCELLED);
        return toResponse(request);
    }

    @Transactional(readOnly = true)
    public List<CustomRequestResponse> mine() {
        return requestRepository.findByBuyerUserIdOrderByCustomRequestIdDesc(currentUser.requireId()).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CustomRequestResponse get(Long id) {
        return toResponse(myRequest(id));
    }

    /** A request of the logged-in customer (404 otherwise). */
    CustomRequest myRequest(Long id) {
        Long userId = currentUser.requireId();
        return requestRepository.findById(id).filter(r -> r.getBuyer().getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CUSTOM_REQUEST_NOT_FOUND, id));
    }

    static void requireStatus(CustomRequest request, CustomRequestStatus... allowed) {
        for (CustomRequestStatus status : allowed) {
            if (request.getStatus() == status) {
                return;
            }
        }
        throw new BusinessRuleException(ErrorCode.CUSTOM_REQUEST_STATUS_INVALID, request.getStatus());
    }

    List<CustomOffer> offers(CustomRequest request) {
        return offerRepository.findByCustomRequestCustomRequestIdOrderByCustomOfferIdAsc(request.getCustomRequestId());
    }

    CustomRequestResponse toResponse(CustomRequest request) {
        return CustomRequestMapper.toResponse(request,
                designRepository.findByCustomRequestCustomRequestIdOrderByDesignIdAsc(request.getCustomRequestId()),
                offers(request));
    }

    private void apply(CustomRequestInput input, CustomRequest request) {
        if (input.shapeId() == null || input.styleId() == null || input.offeredPrice() == null) {
            throw BusinessRuleException.unprocessable(ErrorCode.CUSTOM_REQUEST_INCOMPLETE);
        }
        request.setTitle(input.title().strip());
        request.setDescription(input.description() == null ? "" : input.description().strip());
        request.setShape(shapeRepository.findById(input.shapeId())
                .orElseThrow(() -> BusinessRuleException.unprocessable(ErrorCode.UNKNOWN_CATALOG_ID)));
        request.setStyle(styleRepository.findById(input.styleId())
                .orElseThrow(() -> BusinessRuleException.unprocessable(ErrorCode.UNKNOWN_CATALOG_ID)));
        request.setColors(new HashSet<>(colorRepository.findAllById(input.colorIds() == null ? List.of() : input.colorIds())));
        request.setMotifs(new HashSet<>(motifRepository.findAllById(input.motifIds() == null ? List.of() : input.motifIds())));
        request.setSizeNotes(input.sizeNotes());
        request.setOfferedPrice(input.offeredPrice());
        request.setDesiredDate(date(input.desiredDate()));
        request.setTargetMaster(input.targetMasterId() == null ? null : masterRepository.findById(input.targetMasterId())
                .filter(m -> m.isActive())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MASTER_NOT_FOUND, input.targetMasterId())));
    }

    private static Instant date(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return value.length() <= 10 ? LocalDate.parse(value).atStartOfDay(ZoneOffset.UTC).toInstant() : Instant.parse(value);
        } catch (DateTimeParseException ex) {
            throw BusinessRuleException.unprocessable(ErrorCode.CUSTOM_REQUEST_INCOMPLETE);
        }
    }
}
