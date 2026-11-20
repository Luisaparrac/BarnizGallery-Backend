package com.barnizgallery.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.MasterResponse;
import com.barnizgallery.backend.service.AdminMasterService;
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
    private final AdminMasterService adminMasterService;

    public AdminMasterController(MasterService masterService, AdminMasterService adminMasterService) {
        this.masterService = masterService;
        this.adminMasterService = adminMasterService;
    }

    @PostMapping("/{masterId}/approve")
    @Operation(summary = "Approve an active master: roster masters directly, signed-up artisans when all steps pass")
    public MasterResponse approve(@PathVariable Long masterId) {
        return adminMasterService.approve(masterId);
    }

    @GetMapping
    @Operation(summary = "Every master, active or archived, with verification status and active flag")
    public List<MasterResponse> findAll() {
        return masterService.findAllForAdmin();
    }
}
