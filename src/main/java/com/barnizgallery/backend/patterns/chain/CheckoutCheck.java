package com.barnizgallery.backend.patterns.chain;

import java.util.List;

import com.barnizgallery.backend.entity.CartItem;

/**
 * <b>Chain of Responsibility pattern – Handler.</b>
 * <p>
 * One check of the cart before checkout. Each handler checks one rule and passes the cart to the next one; the
 * first broken rule stops the chain with a 409. New rules are added by linking a new handler, without touching
 * the checkout service.
 */
public abstract class CheckoutCheck {

    private CheckoutCheck next;

    /** Links the next handler and returns it, so a chain reads {@code a.then(b).then(c)}. */
    public CheckoutCheck then(CheckoutCheck nextCheck) {
        this.next = nextCheck;
        return nextCheck;
    }

    /** Runs this check and, if it passes, the rest of the chain. */
    public final void handle(List<CartItem> lines) {
        check(lines);
        if (next != null) {
            next.handle(lines);
        }
    }

    protected abstract void check(List<CartItem> lines);
}
