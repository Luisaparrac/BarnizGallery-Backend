package com.barnizgallery.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.GalleryNodeResponse;

/**
 * Exposes the whole gallery as a tree (Composite pattern).
 */
@Service
public class GalleryService {

    private final GalleryTreeBuilder treeBuilder;

    public GalleryService(GalleryTreeBuilder treeBuilder) {
        this.treeBuilder = treeBuilder;
    }

    @Transactional(readOnly = true)
    public GalleryNodeResponse getGallery() {
        return treeBuilder.build().toNode();
    }
}
