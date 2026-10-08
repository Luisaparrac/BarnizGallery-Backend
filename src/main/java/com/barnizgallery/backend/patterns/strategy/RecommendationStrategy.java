package com.barnizgallery.backend.patterns.strategy;

import java.util.List;

import com.barnizgallery.backend.model.entity.Visitor;

/**
 * <b>Strategy pattern – Strategy.</b>
 * <p>
 * A family of interchangeable algorithms to recommend rooms to a visitor.
 * The client ({@code AiFacade}) only knows this interface and picks one at runtime
 * by name ({@code ?strategy=profile|interactions|hybrid|ai}), so a new algorithm can
 * be added without touching the code that uses it.
 */
public interface RecommendationStrategy {

    /** Rooms recommended to the visitor, best first. Rooms with no match are not included. */
    List<RecommendationResult> recommend(Visitor visitor);

    /** Name used in the query parameter {@code strategy}. */
    String name();
}
