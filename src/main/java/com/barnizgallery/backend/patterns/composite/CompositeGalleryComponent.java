package com.barnizgallery.backend.patterns.composite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.barnizgallery.backend.dto.GalleryNodeResponse;

/**
 * <b>Composite pattern – Composite (shared base).</b>
 * <p>
 * Base class for nodes that contain children ({@link GalleryComposite} and
 * {@link RoomComposite}). The operations are recursive: a composite asks each
 * child and adds the results, without knowing whether the child is a leaf or
 * another composite.
 */
public abstract class CompositeGalleryComponent implements GalleryComponent {

    private final List<GalleryComponent> children = new ArrayList<>();

    public void add(GalleryComponent child) {
        children.add(child);
    }

    public List<GalleryComponent> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public int countArtworks() {
        return children.stream().mapToInt(GalleryComponent::countArtworks).sum();
    }

    @Override
    public int countAvailableForAuction() {
        return children.stream().mapToInt(GalleryComponent::countAvailableForAuction).sum();
    }

    protected List<GalleryNodeResponse> childNodes() {
        return children.stream().map(GalleryComponent::toNode).toList();
    }
}
