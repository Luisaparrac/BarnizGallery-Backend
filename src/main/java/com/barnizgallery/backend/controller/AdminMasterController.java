package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.MasterResponse;
import com.barnizgallery.backend.service.MasterService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Every master with its status, active or archived. Only for administrators.
 */
@RestController
@RequestMapping("/api/admin/masters")
@Tag(name = "Administration")
public class AdminMasterController {

    private final MasterService masterService;

    public AdminMasterController(MasterService masterService) {
        this.masterService = masterService;
    }

    @GetMapping
    @Operation(summary = "Every master, active or archived, with verification status and active flag")
    public List<MasterResponse> findAll() {
        return masterService.findAllForAdmin();
    }
}
