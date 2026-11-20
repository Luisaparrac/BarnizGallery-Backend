package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.CatalogEntryRequest;
import com.barnizgallery.backend.dto.CatalogEntryResponse;
import com.barnizgallery.backend.enums.CatalogKind;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.service.CatalogAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Administration of the simple catalogs: {@code /api/admin/catalogs/{catalog}} where {@code catalog} is one of
 * the {@link CatalogKind} paths. Only for administrators.
 */
@RestController
@RequestMapping("/api/admin/catalogs/{catalog}")
@Tag(name = "Administration")
public class AdminCatalogController {

    private final CatalogAdminService catalogAdminService;

    public AdminCatalogController(CatalogAdminService catalogAdminService) {
        this.catalogAdminService = catalogAdminService;
    }

    @GetMapping
    @Operation(summary = "Every entry of the catalog, active or not")
    public List<CatalogEntryResponse> list(@PathVariable String catalog) {
        return catalogAdminService.list(kind(catalog));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an entry with a new code")
    public CatalogEntryResponse create(@PathVariable String catalog, @Valid @RequestBody CatalogEntryRequest request) {
        return catalogAdminService.create(kind(catalog), request);
    }

    @PutMapping("/{code}")
    @Operation(summary = "Edit names, order, extra fields or active flag of an entry (the code never changes)")
    public CatalogEntryResponse update(@PathVariable String catalog, @PathVariable String code,
            @Valid @RequestBody CatalogEntryRequest request) {
        return catalogAdminService.update(kind(catalog), code, request);
    }

    private static CatalogKind kind(String path) {
        return CatalogKind.fromPath(path)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATALOG_NOT_FOUND, path));
    }
}
