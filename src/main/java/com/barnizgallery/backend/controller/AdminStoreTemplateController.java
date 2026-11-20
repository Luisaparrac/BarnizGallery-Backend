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

import com.barnizgallery.backend.dto.StoreTemplateRequest;
import com.barnizgallery.backend.dto.StoreTemplateResponse;
import com.barnizgallery.backend.service.StoreTemplateAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Administration of the store templates. Only for administrators.
 */
@RestController
@RequestMapping("/api/admin/store-templates")
@Tag(name = "Administration")
public class AdminStoreTemplateController {

    private final StoreTemplateAdminService service;

    public AdminStoreTemplateController(StoreTemplateAdminService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Every store template, active or not")
    public List<StoreTemplateResponse> list() {
        return service.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StoreTemplateResponse create(@Valid @RequestBody StoreTemplateRequest request) {
        return service.create(request);
    }

    @PutMapping("/{code}")
    @Operation(summary = "Edit a template; deactivate it with active=false (the code never changes)")
    public StoreTemplateResponse update(@PathVariable String code, @Valid @RequestBody StoreTemplateRequest request) {
        return service.update(code, request);
    }
}
