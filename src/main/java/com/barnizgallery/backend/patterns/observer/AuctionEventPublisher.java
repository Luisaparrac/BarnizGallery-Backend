package com.barnizgallery.backend.patterns.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * <b>Observer pattern – Subject.</b>
 * <p>
 * When something happens in an auction (a bid, start, finish, cancel), several parts of the
 * system must react: broadcast it live, warn the outbid visitor, record the interaction and
 * notify visitors with matching tastes. The services only call {@link #publish(AuctionEvent)};
 * they do not know who is listening. New reactions are added as new observers, without
 * changing the services.
 * <p>
 * Implemented explicitly (not with Spring's ApplicationEventPublisher) so the pattern is visible.
 */
@Component
public class AuctionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AuctionEventPublisher.class);

    private final List<AuctionObserver> observers = new CopyOnWriteArrayList<>();

    /** Spring passes every {@link AuctionObserver} bean; they are all subscribed at startup. */
    public AuctionEventPublisher(List<AuctionObserver> initialObservers) {
        initialObservers.forEach(this::subscribe);
    }

    public void subscribe(AuctionObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /** Notifies every observer. A failing observer is logged and does not stop the others. */
    public void publish(AuctionEvent event) {
        for (AuctionObserver observer : observers) {
            try {
                observer.onEvent(event);
            } catch (RuntimeException ex) {
                log.error("Observer {} failed handling {} of auction {}", observer.getClass().getSimpleName(),
                        event.type(), event.auctionId(), ex);
            }
        }
    }
}
