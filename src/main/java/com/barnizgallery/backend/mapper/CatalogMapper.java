package com.barnizgallery.backend.mapper;

import com.barnizgallery.backend.dto.BudgetRangeResponse;
import com.barnizgallery.backend.dto.CatalogEntryResponse;
import com.barnizgallery.backend.dto.StoreTemplateResponse;
import com.barnizgallery.backend.entity.Avatar;
import com.barnizgallery.backend.entity.BudgetRange;
import com.barnizgallery.backend.entity.CatalogEntry;
import com.barnizgallery.backend.entity.Color;
import com.barnizgallery.backend.entity.Motif;
import com.barnizgallery.backend.entity.Shape;
import com.barnizgallery.backend.entity.StoreTemplate;
import com.barnizgallery.backend.entity.Style;

/**
 * Converts the catalog entities to their DTOs.
 */
public final class CatalogMapper {

    private CatalogMapper() {
    }

    public static CatalogEntryResponse toResponse(CatalogEntry entry) {
        String hex = entry instanceof Color color ? color.getHex() : null;
        String imageUrl = entry instanceof Avatar avatar ? avatar.getImageUrl() : null;
        return new CatalogEntryResponse(entry.getCode(), entry.getNameEs(), entry.getNameEn(), entry.getSortOrder(),
                entry.isActive(), hex, imageUrl, idOf(entry), entry.isActive());
    }

    /** Database id of a catalog entry (each table names its key differently). */
    static Long idOf(CatalogEntry entry) {
        return switch (entry) {
            case Color c -> c.getColorId();
            case Shape s -> s.getShapeId();
            case Style s -> s.getStyleId();
            case Motif m -> m.getMotifId();
            case Avatar a -> a.getAvatarId();
            default -> null;
        };
    }

    public static BudgetRangeResponse toResponse(BudgetRange range) {
        return new BudgetRangeResponse(range.getCode(), range.getMinAmount(), range.getMaxAmount(),
                range.getCurrency(), range.getBudgetRangeId(), true);
    }

    public static StoreTemplateResponse toResponse(StoreTemplate template) {
        return new StoreTemplateResponse(template.getCode(), template.getNameEs(), template.getNameEn(),
                template.getDescriptionEs(), template.getDescriptionEn(), template.getPreviewUrl(),
                template.getDefaultLayout(), template.getDefaultTheme(), template.isActive(), template.getTemplateId(),
                template.isActive());
    }
}
