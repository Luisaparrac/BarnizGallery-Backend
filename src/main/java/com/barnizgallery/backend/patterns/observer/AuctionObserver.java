package com.barnizgallery.backend.patterns.observer;

/**
 * <b>Observer pattern – Observer.</b>
 * <p>
 * Anything that wants to react to auction events implements this interface.
 * The publisher calls {@link #onEvent(AuctionEvent)} without knowing what each observer does.
 */
public interface AuctionObserver {

    void onEvent(AuctionEvent event);
}
