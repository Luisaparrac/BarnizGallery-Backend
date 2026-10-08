package com.barnizgallery.backend.patterns.facade;

/**
 * Result of reviewing a bid with {@link AiFacade#assessBid}.
 *
 * @param verdict OK (accept), SUSPICIOUS (accept but alert admins) or REJECTED (HTTP 429)
 * @param reason  explanation, null when the verdict is OK
 */
public record BidAssessment(Verdict verdict, String reason) {

    public enum Verdict {
        OK, SUSPICIOUS, REJECTED
    }

    public static BidAssessment ok() {
        return new BidAssessment(Verdict.OK, null);
    }

    public static BidAssessment suspicious(String reason) {
        return new BidAssessment(Verdict.SUSPICIOUS, reason);
    }

    public static BidAssessment rejected(String reason) {
        return new BidAssessment(Verdict.REJECTED, reason);
    }
}
