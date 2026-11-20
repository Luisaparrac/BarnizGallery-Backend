package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Answer of {@code POST /v1/bids/assess}: {@code status} OK or SUSPICIOUS, a {@code score} from 0 to 1, the
 * reasons in Spanish for the administrator and the {@code method} (model or rules). It never rejects a bid.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AiBidAssessment(String status, BigDecimal score, List<String> reasons, String method) {

    public boolean isSuspicious() {
        return "SUSPICIOUS".equalsIgnoreCase(status);
    }
}
