package com.barnizgallery.backend.patterns.strategy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.BudgetRange;
import com.barnizgallery.backend.entity.CatalogEntry;
import com.barnizgallery.backend.entity.TasteProfile;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.mapper.ArtworkMapper;

/**
 * Compares an artwork with a taste profile. Each declared part of the profile has a weight: colors 35, shape 20,
 * style 20, motifs 15 and budget 10. Colors and motifs count by the share of the user's choices the artwork has;
 * shape and style match or not; the budget matches when the price is inside the range, in the same currency.
 * Only the parts the user declared count, so a profile with colors only can still reach 100.
 */
final class ProfileMatcher {

    static final double COLORS = 35;
    static final double SHAPE = 20;
    static final double STYLE = 20;
    static final double MOTIFS = 15;
    static final double BUDGET = 10;

    private ProfileMatcher() {
    }

    static ProfileMatch match(TasteProfile profile, Artwork artwork) {
        Set<String> colors = codes(profile.getColors());
        Set<String> shapes = codes(profile.getShapes());
        Set<String> styles = codes(profile.getStyles());
        Set<String> motifs = codes(profile.getMotifs());
        BudgetRange budget = profile.getBudgetRange();

        List<String> matchedColors = ArtworkMapper.colorCodes(artwork).stream().filter(colors::contains).toList();
        List<String> matchedMotifs = ArtworkMapper.motifCodes(artwork).stream().filter(motifs::contains).toList();
        boolean shape = artwork.getShape() != null && shapes.contains(artwork.getShape().getCode());
        boolean style = artwork.getStyle() != null && styles.contains(artwork.getStyle().getCode());
        boolean inBudget = inBudget(budget, artwork);

        double possible = 0;
        double points = 0;
        if (!colors.isEmpty()) {
            possible += COLORS;
            points += COLORS * matchedColors.size() / colors.size();
        }
        if (!shapes.isEmpty()) {
            possible += SHAPE;
            points += shape ? SHAPE : 0;
        }
        if (!styles.isEmpty()) {
            possible += STYLE;
            points += style ? STYLE : 0;
        }
        if (!motifs.isEmpty()) {
            possible += MOTIFS;
            points += MOTIFS * matchedMotifs.size() / motifs.size();
        }
        if (budget != null) {
            possible += BUDGET;
            points += inBudget ? BUDGET : 0;
        }
        double score = possible == 0 ? 0 : 100.0 * points / possible;
        return new ProfileMatch(artwork, score, matchedColors, shape, style, matchedMotifs, inBudget);
    }

    /** "Coincide con: colores rojo, verde; tu forma preferida; ..." in the user's language. */
    static String reason(User user, ProfileMatch match) {
        List<String> parts = new ArrayList<>();
        if (!match.colors().isEmpty()) {
            String codes = String.join(", ", match.colors());
            parts.add(LocalizedText.pick(user, "colores " + codes, "colors " + codes));
        }
        if (match.shape()) {
            parts.add(LocalizedText.pick(user, "tu forma preferida", "your preferred shape"));
        }
        if (match.style()) {
            parts.add(LocalizedText.pick(user, "tu estilo preferido", "your preferred style"));
        }
        if (!match.motifs().isEmpty()) {
            String codes = String.join(", ", match.motifs());
            parts.add(LocalizedText.pick(user, "figuras " + codes, "motifs " + codes));
        }
        if (match.budget()) {
            parts.add(LocalizedText.pick(user, "tu presupuesto", "your budget"));
        }
        return LocalizedText.pick(user, "Coincide con: ", "Matches: ") + String.join("; ", parts);
    }

    private static boolean inBudget(BudgetRange budget, Artwork artwork) {
        BigDecimal price = artwork.getPrice();
        if (budget == null || price == null || artwork.getCurrency() == null
                || !artwork.getCurrency().equals(budget.getCurrency())) {
            return false;
        }
        return price.compareTo(budget.getMinAmount()) >= 0
                && (budget.getMaxAmount() == null || price.compareTo(budget.getMaxAmount()) <= 0);
    }

    private static Set<String> codes(Set<? extends CatalogEntry> entries) {
        return entries.stream().map(CatalogEntry::getCode).collect(Collectors.toSet());
    }
}
