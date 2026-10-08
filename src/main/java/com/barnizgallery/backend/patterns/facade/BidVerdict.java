package com.barnizgallery.backend.patterns.facade;

/**
 * Decision of {@link AiFacade#assessBid}: accept, accept but alert admins, or reject (HTTP 429).
 */
public enum BidVerdict {
    OK, SUSPICIOUS, REJECTED
}
