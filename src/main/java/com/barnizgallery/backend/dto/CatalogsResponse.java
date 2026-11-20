package com.barnizgallery.backend.dto;

import java.util.List;

/**
 * Every catalog of the application in one answer, for the forms of the frontend (codes and names in both
 * languages).
 */
public record CatalogsResponse(
        List<CatalogEntryResponse> colors,
        List<CatalogEntryResponse> shapes,
        List<CatalogEntryResponse> motifs,
        List<CatalogEntryResponse> styles,
        List<BudgetRangeResponse> budgetRanges,
        List<CatalogEntryResponse> avatars,
        List<StoreTemplateResponse> storeTemplates,
        List<BudgetRangeResponse> budgets,
        List<StoreTemplateResponse> templates) {
}
