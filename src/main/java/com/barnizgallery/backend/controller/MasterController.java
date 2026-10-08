package com.barnizgallery.backend.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.request.MasterRequest;
import com.barnizgallery.backend.dto.response.MasterDetailResponse;
import com.barnizgallery.backend.dto.response.MasterResponse;
import com.barnizgallery.backend.service.MasterService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Masters of Barniz de Pasto. There is no DELETE endpoint on purpose.
 */
@RestController
@RequestMapping("/api/masters")
@Tag(name = "Masters")
public class MasterController {

    private final MasterService masterService;

    public MasterController(MasterService masterService) {
        this.masterService = masterService;
    }

    @GetMapping
    public List<MasterResponse> findAll() {
        return masterService.findAll();
    }

    @GetMapping("/{id}")
    public MasterDetailResponse findById(@PathVariable Integer id) {
        return masterService.findById(id);
    }

    @PostMapping
    public ResponseEntity<MasterResponse> create(@Valid @RequestBody MasterRequest request) {
        MasterResponse created = masterService.create(request);
        return ResponseEntity.created(URI.create("/api/masters/" + created.masterId())).body(created);
    }

    @PutMapping("/{id}")
    public MasterResponse update(@PathVariable Integer id, @Valid @RequestBody MasterRequest request) {
        return masterService.update(id, request);
    }
}
