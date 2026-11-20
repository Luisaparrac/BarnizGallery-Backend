package com.barnizgallery.backend.patterns.strategy;

import java.util.List;

import com.barnizgallery.backend.entity.Artwork;

/**
 * How well one artwork matches a taste profile: a score from 0 to 100 and what matched.
 */
record ProfileMatch(Artwork artwork, double score, List<String> colors, boolean shape, boolean style,
        List<String> motifs, boolean budget) {

    boolean matchesSomething() {
        return score > 0;
    }
}
