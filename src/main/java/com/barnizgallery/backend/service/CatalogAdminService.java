package com.barnizgallery.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.CatalogEntryRequest;
import com.barnizgallery.backend.dto.CatalogEntryResponse;
import com.barnizgallery.backend.entity.CatalogEntry;
import com.barnizgallery.backend.entity.Color;
import com.barnizgallery.backend.enums.CatalogKind;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.CatalogMapper;
import com.barnizgallery.backend.repository.CatalogEntryRepository;
import com.barnizgallery.backend.repository.ColorRepository;

/**
 * Administration of the simple catalogs. Entries are created with a unique code and edited by code; the code
 * never changes and entries are never deleted (artworks and profiles point to them): they are deactivated with
 * {@code active=false} and then disappear from {@code /api/catalogs} and from new requests.
 */
@Service
public class CatalogAdminService {

    private final ColorRepository colorRepository;

    public CatalogAdminService(ColorRepository colorRepository) {
        this.colorRepository = colorRepository;
    }

    /** Every entry, active or not. */
    @Transactional(readOnly = true)
    public List<CatalogEntryResponse> list(CatalogKind kind) {
        return repository(kind).findAllByOrderBySortOrderAscCodeAsc().stream()
                .map(CatalogMapper::toResponse)
                .toList();
    }

    @Transactional
    public CatalogEntryResponse create(CatalogKind kind, CatalogEntryRequest request) {
        if (request.code() == null) {
            throw BusinessRuleException.unprocessable(ErrorCode.CATALOG_CODE_REQUIRED);
        }
        CatalogEntryRepository<CatalogEntry> repository = repository(kind);
        if (repository.findByCode(request.code()).isPresent()) {
            throw new BusinessRuleException(ErrorCode.CATALOG_CODE_EXISTS, request.code());
        }
        CatalogEntry entry = newEntry(kind);
        entry.setCode(request.code());
        apply(entry, request);
        return CatalogMapper.toResponse(repository.save(entry));
    }

    @Transactional
    public CatalogEntryResponse update(CatalogKind kind, String code, CatalogEntryRequest request) {
        CatalogEntry entry = repository(kind).findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATALOG_ENTRY_NOT_FOUND, code));
        apply(entry, request);
        return CatalogMapper.toResponse(entry);
    }

    private static void apply(CatalogEntry entry, CatalogEntryRequest request) {
        entry.setNameEs(request.nameEs().trim());
        entry.setNameEn(request.nameEn().trim());
        entry.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        entry.setActive(request.active() == null || request.active());
        if (entry instanceof Color color) {
            color.setHex(request.hex());
        }
    }

    @SuppressWarnings("unchecked")
    private CatalogEntryRepository<CatalogEntry> repository(CatalogKind kind) {
        CatalogEntryRepository<? extends CatalogEntry> repository = switch (kind) {
            case COLORS -> colorRepository;
        };
        return (CatalogEntryRepository<CatalogEntry>) repository;
    }

    private static CatalogEntry newEntry(CatalogKind kind) {
        return switch (kind) {
            case COLORS -> new Color();
        };
    }
}
