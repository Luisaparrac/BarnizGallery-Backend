package com.barnizgallery.backend.mapper;

import com.barnizgallery.backend.dto.CatalogArtworkResponse;
import com.barnizgallery.backend.entity.ArtworkCatalogView;

/**
 * Converts rows of the public catalog view to their DTO.
 */
public final class CatalogArtworkMapper {

    private CatalogArtworkMapper() {
    }

    public static CatalogArtworkResponse toResponse(ArtworkCatalogView row) {
        return new CatalogArtworkResponse(row.getArtworkId(), row.getSlug(), row.getTitleEs(), row.getTitleEn(),
                row.getTechnique(), row.getSaleMode(), row.getPrice(), row.getCurrency(), row.getStock(),
                row.getArtworkStatus(), row.getStoreId(), row.getStoreSlug(), row.getStoreNameEs(),
                row.getStoreNameEn(), row.getMasterId(), row.getMasterName(), row.getPhotos(), row.getColorCodes(),
                row.getMotifCodes());
    }
}
