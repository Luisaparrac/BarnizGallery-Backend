package com.barnizgallery.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.StoreTemplateRequest;
import com.barnizgallery.backend.dto.StoreTemplateResponse;
import com.barnizgallery.backend.entity.StoreTemplate;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.ResourceNotFoundException;
import com.barnizgallery.backend.mapper.CatalogMapper;
import com.barnizgallery.backend.repository.StoreTemplateRepository;

import tools.jackson.databind.JsonNode;

/**
 * Administration of the store templates. Stores point to their template, so templates are deactivated instead
 * of deleted; the code never changes.
 */
@Service
public class StoreTemplateAdminService {

    private final StoreTemplateRepository repository;

    public StoreTemplateAdminService(StoreTemplateRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<StoreTemplateResponse> list() {
        return repository.findAllByOrderByCodeAsc().stream().map(CatalogMapper::toResponse).toList();
    }

    @Transactional
    public StoreTemplateResponse create(StoreTemplateRequest request) {
        if (request.code() == null) {
            throw BusinessRuleException.unprocessable(ErrorCode.CATALOG_CODE_REQUIRED);
        }
        if (repository.findByCode(request.code()).isPresent()) {
            throw new BusinessRuleException(ErrorCode.CATALOG_CODE_EXISTS, request.code());
        }
        StoreTemplate template = new StoreTemplate();
        template.setCode(request.code());
        apply(template, request);
        return CatalogMapper.toResponse(repository.save(template));
    }

    @Transactional
    public StoreTemplateResponse update(String code, StoreTemplateRequest request) {
        StoreTemplate template = repository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATALOG_ENTRY_NOT_FOUND, code));
        apply(template, request);
        return CatalogMapper.toResponse(template);
    }

    private static void apply(StoreTemplate template, StoreTemplateRequest request) {
        template.setNameEs(request.nameEs().trim());
        template.setNameEn(request.nameEn().trim());
        template.setDescriptionEs(request.descriptionEs());
        template.setDescriptionEn(request.descriptionEn());
        template.setPreviewUrl(request.previewUrl());
        template.setDefaultLayout(jsonObject(request.defaultLayout()));
        template.setDefaultTheme(jsonObject(request.defaultTheme()));
        template.setActive(request.active() == null || request.active());
    }

    /** The JSON object as text; a missing value is {@code {}} and any other JSON value is refused (422). */
    private static String jsonObject(JsonNode value) {
        if (value == null || value.isNull()) {
            return "{}";
        }
        if (!value.isObject()) {
            throw BusinessRuleException.unprocessable(ErrorCode.TEMPLATE_JSON_INVALID);
        }
        return value.toString();
    }
}
