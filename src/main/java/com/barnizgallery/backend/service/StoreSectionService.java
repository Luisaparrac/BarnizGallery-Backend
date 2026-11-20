package com.barnizgallery.backend.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.StoreSectionRequest;
import com.barnizgallery.backend.dto.StoreSectionResponse;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.entity.StoreSection;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;
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

    /** Shows or hides a section; hidden sections stay in the editor but not on the public page. */
    @Transactional
    public StoreSectionResponse setVisible(Long sectionId, boolean visible) {
        StoreSection section = mine(sectionId);
        section.setVisible(visible);
        return StoreEditorMapper.toResponse(section);
    }

    /**
     * Puts the sections in the given order. The list must name every section of the store exactly once (422
     * otherwise).
     */
    @Transactional
    public List<StoreSectionResponse> reorder(List<Long> sectionIds) {
        List<StoreSection> sections = sections(editorService.myStore());
        Map<Long, StoreSection> byId = new HashMap<>();
        sections.forEach(section -> byId.put(section.getStoreSectionId(), section));
        if (sectionIds.size() != sections.size() || !byId.keySet().equals(new HashSet<>(sectionIds))) {
            throw BusinessRuleException.unprocessable(ErrorCode.SECTION_ORDER_INVALID);
        }
        for (int i = 0; i < sectionIds.size(); i++) {
            byId.get(sectionIds.get(i)).setSortOrder(i);
        }
        return sectionIds.stream().map(byId::get).map(StoreEditorMapper::toResponse).toList();
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
