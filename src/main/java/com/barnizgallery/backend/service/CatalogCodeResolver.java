package com.barnizgallery.backend.service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.entity.Color;
import com.barnizgallery.backend.entity.Motif;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.repository.ColorRepository;
import com.barnizgallery.backend.repository.MotifRepository;

/**
 * Turns catalog codes sent by the clients (for example {@code "rojo"}) into catalog entities.
 * An unknown code is rejected with 422; nothing is created on the fly.
 */
@Component
public class CatalogCodeResolver {

    private final ColorRepository colorRepository;
    private final MotifRepository motifRepository;

    public CatalogCodeResolver(ColorRepository colorRepository, MotifRepository motifRepository) {
        this.colorRepository = colorRepository;
        this.motifRepository = motifRepository;
    }

    @Transactional(readOnly = true)
    public Set<Color> colors(Collection<String> codes) {
        return resolve("color", codes, colorRepository::findByCodeIn, Color::getCode);
    }

    @Transactional(readOnly = true)
    public Set<Motif> motifs(Collection<String> codes) {
        return resolve("motif", codes, motifRepository::findByCodeIn, Motif::getCode);
    }

    private static <T> Set<T> resolve(String kind, Collection<String> codes,
            Function<Collection<String>, List<T>> finder, Function<T, String> codeOf) {
        if (codes == null || codes.isEmpty()) {
            return new LinkedHashSet<>();
        }
        Set<String> wanted = new LinkedHashSet<>(codes);
        List<T> found = finder.apply(wanted);
        Set<String> missing = new LinkedHashSet<>(wanted);
        found.forEach(item -> missing.remove(codeOf.apply(item)));
        if (!missing.isEmpty()) {
            throw BusinessRuleException.unprocessable("Unknown " + kind + " codes: " + missing);
        }
        return new LinkedHashSet<>(found);
    }
}
