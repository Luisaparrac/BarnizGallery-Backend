package com.barnizgallery.backend.entity;

/**
 * What the simple catalogs have in common (colors, shapes, motifs, styles and avatars): a stable code, a name in
 * Spanish and English, a position in the lists and an active flag. The API always refers to them by code.
 */
public interface CatalogEntry {

    String getCode();

    void setCode(String code);

    String getNameEs();

    void setNameEs(String nameEs);

    String getNameEn();

    void setNameEn(String nameEn);

    Integer getSortOrder();

    void setSortOrder(Integer sortOrder);

    boolean isActive();

    void setActive(boolean active);
}
