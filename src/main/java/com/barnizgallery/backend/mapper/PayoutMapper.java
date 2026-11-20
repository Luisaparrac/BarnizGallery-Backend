package com.barnizgallery.backend.mapper;

import com.barnizgallery.backend.dto.PayoutResponse;
import com.barnizgallery.backend.entity.Payout;

/**
 * Converts payouts to their DTO.
 */
public final class PayoutMapper {

    private PayoutMapper() {
    }

    public static PayoutResponse toResponse(Payout payout) {
        return new PayoutResponse(payout.getPayoutId(), payout.getMaster().getMasterId(), payout.getMaster().getName(),
                payout.getCurrency(), payout.getAmount(), payout.getStatus(), payout.getReference(),
                payout.getCreatedAt(), payout.getPaidAt());
    }
}
