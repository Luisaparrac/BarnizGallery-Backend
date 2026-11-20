package com.barnizgallery.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.StoreSectionRequest;
import com.barnizgallery.backend.dto.StoreSectionResponse;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.entity.StoreSection;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.StoreEditorMapper;
import com.barnizgallery.backend.repository.StoreSectionRepository;

/**
 * Sections of the logged-in artisan's store. A section of another store is answered as not found.
 */
@Service
public class StoreSectionService {

    private final StoreSectionRepository sectionRepository;
    private final StoreEditorService editorService;

    public StoreSectionService(StoreSectionRepository sectionRepository, StoreEditorService editorService) {
        this.sectionRepository = sectionRepository;
        this.editorService = editorService;
    }

    /** Adds a visible section at the end of the page. */
    @Transactional
    public StoreSectionResponse add(StoreSectionRequest request) {
        Store store = editorService.myStore();
        List<StoreSection> sections = sections(store);
        StoreSection section = new StoreSection();
        section.setStore(store);
        section.setSortOrder(sections.isEmpty() ? 0 : sections.get(sections.size() - 1).getSortOrder() + 1);
        section.setVisible(true);
        apply(section, request);
        return StoreEditorMapper.toResponse(sectionRepository.save(section));
    }

    /** Replaces the content of a section; its position and visibility do not change. */
    @Transactional
    public StoreSectionResponse update(Long sectionId, StoreSectionRequest request) {
        StoreSection section = mine(sectionId);
        apply(section, request);
        return StoreEditorMapper.toResponse(section);
    }

    StoreSection mine(Long sectionId) {
        Store store = editorService.myStore();
        return sectionRepository.findById(sectionId)
                .filter(section -> section.getStore().getStoreId().equals(store.getStoreId()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.STORE_SECTION_NOT_FOUND, sectionId));
    }

    List<StoreSection> sections(Store store) {
        return sectionRepository.findByStoreStoreIdOrderBySortOrderAscStoreSectionIdAsc(store.getStoreId());
    }

    private static void apply(StoreSection section, StoreSectionRequest request) {
        section.setSectionType(request.sectionType());
        section.setTitleEs(request.titleEs());
        section.setTitleEn(request.titleEn());
        section.setContentEs(request.contentEs());
        section.setContentEn(request.contentEn());
        section.setImageUrl(request.imageUrl());
        section.setConfig(request.config() == null || request.config().isNull() ? null
                : StoreEditorService.jsonObject(request.config()));
    }
}
