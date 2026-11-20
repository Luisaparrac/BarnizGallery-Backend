package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.BudgetRangeRequest;
import com.barnizgallery.backend.dto.BudgetRangeResponse;
import com.barnizgallery.backend.service.BudgetRangeAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Administration of the budget ranges. Only for administrators.
 */
@RestController
@RequestMapping("/api/admin/catalogs/budget-ranges")
@Tag(name = "Administration")
public class AdminBudgetRangeController {

    private final BudgetRangeAdminService service;

    public AdminBudgetRangeController(BudgetRangeAdminService service) {
        this.service = service;
    }

    @GetMapping
    public List<BudgetRangeResponse> list() {
        return service.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a budget range (409 if the code exists, 422 if max < min)")
    public BudgetRangeResponse create(@Valid @RequestBody BudgetRangeRequest request) {
        return service.create(request);
    }

    @PutMapping("/{code}")
    public BudgetRangeResponse update(@PathVariable String code, @Valid @RequestBody BudgetRangeRequest request) {
        return service.update(code, request);
    }

    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an unused budget range (409 while a taste profile uses it)")
    public void delete(@PathVariable String code) {
        service.delete(code);
    }
}
