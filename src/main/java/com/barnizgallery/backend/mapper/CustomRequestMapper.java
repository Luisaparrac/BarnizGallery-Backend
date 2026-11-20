package com.barnizgallery.backend.mapper;

import java.util.List;

import com.barnizgallery.backend.dto.CustomDesignResponse;
import com.barnizgallery.backend.dto.CustomOfferResponse;
import com.barnizgallery.backend.dto.CustomRequestResponse;
import com.barnizgallery.backend.entity.Color;
import com.barnizgallery.backend.entity.CustomOffer;
import com.barnizgallery.backend.entity.CustomRequest;
import com.barnizgallery.backend.entity.CustomRequestDesign;
import com.barnizgallery.backend.entity.Motif;
import com.barnizgallery.backend.enums.OfferStatus;
import com.barnizgallery.backend.enums.OfferType;

/**
 * Converts custom requests, designs and offers to the shape the frontend reads.
 */
public final class CustomRequestMapper {

    private CustomRequestMapper() {
    }

    public static CustomRequestResponse toResponse(CustomRequest r, List<CustomRequestDesign> designs,
            List<CustomOffer> offers) {
        boolean withOffers = offers.stream().anyMatch(o -> o.getStatus() == OfferStatus.ACTIVE);
        return new CustomRequestResponse(r.getCustomRequestId(), r.getBuyer().getUserId(), r.getBuyer().getName(),
                r.getTargetMaster() == null ? null : r.getTargetMaster().getMasterId(), r.getTitle(), r.getDescription(),
                r.getShape() == null ? null : r.getShape().getShapeId(),
                r.getStyle() == null ? null : r.getStyle().getStyleId(),
                r.getColors().stream().map(Color::getColorId).sorted().toList(),
                r.getMotifs().stream().map(Motif::getMotifId).sorted().toList(), r.getSizeNotes(), r.getOfferedPrice(),
                r.getCurrency(), r.getDesiredDate(), frontendStatus(r, withOffers), r.getStatus(),
                r.getAcceptedOffer() == null ? null : r.getAcceptedOffer().getCustomOfferId(),
                designs.stream().map(CustomRequestMapper::toResponse).toList(),
                offers.stream().map(CustomRequestMapper::toResponse).toList(), r.getExpiresAt(), r.getCreatedAt(), null);
    }

    static String frontendStatus(CustomRequest r, boolean withOffers) {
        return switch (r.getStatus()) {
            case DRAFT -> "DRAFT";
            case OPEN -> withOffers ? "WITH_OFFERS" : "PUBLISHED";
            case ACCEPTED -> "ACCEPTED";
            case IN_PRODUCTION, FINISHED -> "IN_PRODUCTION";
            case COMPLETED -> "DELIVERED";
            case CANCELLED -> "CANCELLED";
            case EXPIRED -> "EXPIRED";
        };
    }

    public static CustomDesignResponse toResponse(CustomRequestDesign d) {
        return new CustomDesignResponse(d.getDesignId(), d.getProvider(), d.getModel(), d.getPrompt(), d.getStatus(),
                d.getImageUrl(), d.isSelected(), d.getCreatedAt());
    }

    public static CustomOfferResponse toResponse(CustomOffer o) {
        return new CustomOfferResponse(o.getCustomOfferId(), o.getCustomRequest().getCustomRequestId(),
                o.getMaster().getMasterId(), o.getMaster().getName(), o.getStore().getSlug(),
                o.getOfferType() == OfferType.ACCEPTS_PRICE ? "ACCEPT_PRICE" : "COUNTER_OFFER", o.getPrice(),
                o.getCurrency(), o.getEstimatedDays(), o.getMessage(), null,
                o.getStatus() == OfferStatus.ACTIVE ? "SENT" : o.getStatus().name(), o.getCreatedAt());
    }
}
