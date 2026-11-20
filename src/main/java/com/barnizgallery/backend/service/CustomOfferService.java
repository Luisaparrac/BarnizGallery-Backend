package com.barnizgallery.backend.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.AcceptOfferResponse;
import com.barnizgallery.backend.dto.CustomRequestResponse;
import com.barnizgallery.backend.dto.OfferInput;
import com.barnizgallery.backend.entity.CustomOffer;
import com.barnizgallery.backend.entity.CustomRequest;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.entity.Order;
import com.barnizgallery.backend.enums.CustomRequestStatus;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.OfferStatus;
import com.barnizgallery.backend.enums.OfferType;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.repository.CustomOfferRepository;
import com.barnizgallery.backend.repository.CustomRequestRepository;

/**
 * Offers on custom requests. Approved artisans send offers on open requests (open to all or addressed to them) and
 * withdraw their own; the customer accepts one (the others are rejected and an {@code encargo} order with deposit and
 * balance is created) or rejects it. Every step notifies the other side.
 */
@Service
public class CustomOfferService {

    static final String NEW_CUSTOM_OFFER = "new_custom_offer";
    static final String OFFER_ACCEPTED = "offer_accepted";
    static final String OFFER_REJECTED = "offer_rejected";

    private final CustomRequestRepository requestRepository;
    private final CustomOfferRepository offerRepository;
    private final CustomRequestService requestService;
    private final ApprovedArtisan approvedArtisan;
    private final CustomOrderCreator orderCreator;
    private final NotificationService notificationService;

    public CustomOfferService(CustomRequestRepository requestRepository, CustomOfferRepository offerRepository,
            CustomRequestService requestService, ApprovedArtisan approvedArtisan, CustomOrderCreator orderCreator,
            NotificationService notificationService) {
        this.requestRepository = requestRepository;
        this.offerRepository = offerRepository;
        this.requestService = requestService;
        this.approvedArtisan = approvedArtisan;
        this.orderCreator = orderCreator;
        this.notificationService = notificationService;
    }

    @Transactional
    public CustomRequestResponse send(Long requestId, OfferInput input) {
        Master master = approvedArtisan.master();
        CustomRequest request = visibleTo(master, requestId);
        CustomRequestService.requireStatus(request, CustomRequestStatus.OPEN);
        CustomOffer offer = new CustomOffer();
        offer.setCustomRequest(request);
        offer.setMaster(master);
        offer.setStore(approvedArtisan.store(master));
        boolean acceptsPrice = "ACCEPT_PRICE".equals(input.type());
        offer.setOfferType(acceptsPrice ? OfferType.ACCEPTS_PRICE : OfferType.COUNTER_OFFER);
        BigDecimal price = acceptsPrice || input.price() == null ? request.getOfferedPrice() : input.price();
        offer.setPrice(price);
        offer.setCurrency(request.getCurrency());
        offer.setEstimatedDays(input.estimatedDays());
        offer.setMessage(input.message());
        offer.setStatus(OfferStatus.ACTIVE);
        offerRepository.save(offer);
        notificationService.notify(request.getBuyer(), NEW_CUSTOM_OFFER, "/custom-requests/" + requestId, null,
                master.getName(), request.getTitle());
        return requestService.toResponse(request);
    }

    /** The artisan withdraws an own active offer; it is stored as expired (no withdrawn state in the database). */
    @Transactional
    public CustomRequestResponse withdraw(Long requestId, Long offerId) {
        Master master = approvedArtisan.master();
        CustomOffer offer = offer(requestId, offerId);
        if (!offer.getMaster().getMasterId().equals(master.getMasterId())) {
            throw new ResourceNotFoundException(ErrorCode.CUSTOM_OFFER_NOT_FOUND, offerId);
        }
        requireActive(offer);
        offer.setStatus(OfferStatus.EXPIRED);
        return requestService.toResponse(offer.getCustomRequest());
    }

    @Transactional
    public CustomRequestResponse reject(Long requestId, Long offerId) {
        CustomRequest request = requestService.myRequest(requestId);
        CustomOffer offer = offer(requestId, offerId);
        requireActive(offer);
        offer.setStatus(OfferStatus.REJECTED);
        notifyArtisan(offer, OFFER_REJECTED, request);
        return requestService.toResponse(request);
    }

    /** Accepts an active offer of an open request: the others are rejected and the {@code encargo} order is created. */
    @Transactional
    public AcceptOfferResponse accept(Long requestId, Long offerId) {
        CustomRequest request = requestService.myRequest(requestId);
        CustomRequestService.requireStatus(request, CustomRequestStatus.OPEN);
        CustomOffer accepted = offer(requestId, offerId);
        requireActive(accepted);
        for (CustomOffer other : requestService.offers(request)) {
            if (other.getStatus() == OfferStatus.ACTIVE && !other.getCustomOfferId().equals(offerId)) {
                other.setStatus(OfferStatus.REJECTED);
                notifyArtisan(other, OFFER_REJECTED, request);
            }
        }
        accepted.setStatus(OfferStatus.ACCEPTED);
        request.setAcceptedOffer(accepted);
        request.setStatus(CustomRequestStatus.ACCEPTED);
        requestRepository.saveAndFlush(request);
        Order order = orderCreator.create(request, accepted);
        notifyArtisan(accepted, OFFER_ACCEPTED, request);
        return new AcceptOfferResponse(requestService.toResponse(request), order.getOrderId());
    }

    private CustomRequest visibleTo(Master master, Long requestId) {
        return requestRepository.findById(requestId)
                .filter(r -> r.getTargetMaster() == null || r.getTargetMaster().getMasterId().equals(master.getMasterId()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CUSTOM_REQUEST_NOT_FOUND, requestId));
    }

    private CustomOffer offer(Long requestId, Long offerId) {
        return offerRepository.findById(offerId)
                .filter(o -> o.getCustomRequest().getCustomRequestId().equals(requestId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CUSTOM_OFFER_NOT_FOUND, offerId));
    }

    private static void requireActive(CustomOffer offer) {
        if (offer.getStatus() != OfferStatus.ACTIVE) {
            throw new com.barnizgallery.backend.exception.BusinessRuleException(ErrorCode.CUSTOM_OFFER_NOT_ACTIVE,
                    offer.getCustomOfferId());
        }
    }

    private void notifyArtisan(CustomOffer offer, String type, CustomRequest request) {
        if (offer.getMaster().getUser() != null) {
            notificationService.notify(offer.getMaster().getUser(), type, "/artisan/custom-requests", null,
                    request.getTitle());
        }
    }
}
