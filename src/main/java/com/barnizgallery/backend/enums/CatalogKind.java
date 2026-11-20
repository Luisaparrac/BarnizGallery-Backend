package com.barnizgallery.backend.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * The simple catalogs an administrator can edit, by the name used in the path
 * ({@code /api/admin/catalogs/<path>}).
 */
public enum CatalogKind {

    COLORS("colors"),
    SHAPES("shapes"),
    MOTIFS("motifs");

    private final String path;

    CatalogKind(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }

    public static Optional<CatalogKind> fromPath(String path) {
        return Arrays.stream(values()).filter(kind -> kind.path.equals(path)).findFirst();
    }
}
