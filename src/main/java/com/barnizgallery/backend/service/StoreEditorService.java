package com.barnizgallery.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.CreateStoreRequest;
import com.barnizgallery.backend.dto.StoreEditorResponse;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.entity.StoreSection;
import com.barnizgallery.backend.entity.StoreTemplate;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.enums.StoreStatus;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.StoreEditorMapper;
import com.barnizgallery.backend.repository.MasterRepository;
import com.barnizgallery.backend.repository.StoreRepository;
import com.barnizgallery.backend.repository.StoreSectionRepository;
import com.barnizgallery.backend.repository.StoreTemplateRepository;
import com.barnizgallery.backend.security.CurrentUser;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The logged-in artisan's store editor. A master has one store; it starts as a {@code borrador} copy of a
 * template: the template theme and the sections listed in its {@code default_layout}
 * ({@code {"sections": [{"type", "titleEs", "titleEn", "contentEs", "contentEn", "imageUrl", "config"}]}}).
 */
@Service
public class StoreEditorService {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final StoreRepository storeRepository;
    private final StoreTemplateRepository templateRepository;
    private final StoreSectionRepository sectionRepository;
    private final MasterRepository masterRepository;
    private final CurrentUser currentUser;

    public StoreEditorService(StoreRepository storeRepository, StoreTemplateRepository templateRepository,
            StoreSectionRepository sectionRepository, MasterRepository masterRepository, CurrentUser currentUser) {
        this.storeRepository = storeRepository;
        this.templateRepository = templateRepository;
        this.sectionRepository = sectionRepository;
        this.masterRepository = masterRepository;
        this.currentUser = currentUser;
    }

    /** Creates the artisan's store from an active template (409 when the master already has one). */
    @Transactional
    public StoreEditorResponse create(CreateStoreRequest request) {
        Master master = myMaster();
        if (storeRepository.existsByMasterMasterId(master.getMasterId())) {
            throw new BusinessRuleException(ErrorCode.MASTER_ALREADY_HAS_STORE, master.getMasterId());
        }
        StoreTemplate template = templateRepository.findByCode(request.templateCode())
                .filter(StoreTemplate::isActive)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.STORE_TEMPLATE_NOT_FOUND,
                        request.templateCode()));
        Store store = new Store();
        store.setMaster(master);
        store.setTemplate(template);
        store.setNameEs(request.nameEs().trim());
        store.setNameEn(request.nameEn().trim());
        store.setDescriptionEs(request.descriptionEs());
        store.setDescriptionEn(request.descriptionEn());
        store.setSlug(freeSlug(request.slug() != null ? request.slug() : SlugGenerator.slugOf(request.nameEs())));
        store.setTheme(template.getDefaultTheme());
        store.setAcceptsCustomOrders(Boolean.TRUE.equals(request.acceptsCustomOrders()));
        store.setStatus(StoreStatus.DRAFT);
        Store saved = storeRepository.save(store);
        List<StoreSection> sections = sectionRepository.saveAll(sectionsOf(template, saved));
        return StoreEditorMapper.toResponse(saved, sections);
    }

    /** The artisan's store with all its sections. */
    @Transactional(readOnly = true)
    public StoreEditorResponse mine() {
        Store store = myStore();
        return StoreEditorMapper.toResponse(store,
                sectionRepository.findByStoreStoreIdOrderBySortOrderAscStoreSectionIdAsc(store.getStoreId()));
    }

    /** The store of the logged-in artisan (404 when they have none yet). */
    Store myStore() {
        Master master = myMaster();
        return storeRepository.findFirstByMasterMasterIdOrderByStoreIdAsc(master.getMasterId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ARTISAN_STORE_NOT_FOUND,
                        master.getMasterId()));
    }

    private Master myMaster() {
        Long userId = currentUser.requireId();
        return masterRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ARTISAN_PROFILE_NOT_FOUND, userId));
    }

    private String freeSlug(String wanted) {
        return SlugGenerator.unique(wanted, storeRepository::existsBySlug);
    }

    private static List<StoreSection> sectionsOf(StoreTemplate template, Store store) {
        List<StoreSection> sections = new ArrayList<>();
        JsonNode layout;
        try {
            layout = JSON.readTree(template.getDefaultLayout());
        } catch (JacksonException ex) {
            return sections;
        }
        int order = 0;
        for (JsonNode item : layout.path("sections").values()) {
            String type = item.path("type").asString();
            if (type.isBlank()) {
                continue;
            }
            StoreSection section = new StoreSection();
            section.setStore(store);
            section.setSectionType(type);
            section.setTitleEs(text(item, "titleEs"));
            section.setTitleEn(text(item, "titleEn"));
            section.setContentEs(text(item, "contentEs"));
            section.setContentEn(text(item, "contentEn"));
            section.setImageUrl(text(item, "imageUrl"));
            section.setConfig(item.path("config").isObject() ? item.path("config").toString() : null);
            section.setSortOrder(order++);
            section.setVisible(true);
            sections.add(section);
        }
        return sections;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isString() ? value.asString() : null;
    }
}
