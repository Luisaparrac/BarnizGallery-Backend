package com.barnizgallery.backend.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

/**
 * The new order of the store sections: every section id of the store, once, from top to bottom.
 */
public record SectionOrderRequest(@NotEmpty List<Long> sectionIds) {
}
