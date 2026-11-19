package com.barnizgallery.backend.patterns.strategy;

import java.util.List;

import com.barnizgallery.backend.entity.User;

/**
 * <b>Strategy pattern – Strategy.</b>
 * <p>
 * A family of interchangeable algorithms to recommend stores to a user.
 * The client ({@code AiFacade}) only knows this interface and picks one at runtime
 * by name ({@code ?strategy=profile|interactions|hybrid|ai}), so a new algorithm can
 * be added without touching the code that uses it.
 */
public interface RecommendationStrategy {

    /** Stores recommended to the user, best first. Stores with no match are not included. */
    List<RecommendationResult> recommend(User user);

    /** Name used in the query parameter {@code strategy}. */
    String name();
}
