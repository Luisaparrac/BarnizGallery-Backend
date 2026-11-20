package com.barnizgallery.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.barnizgallery.backend.dto.ReportRequest;
import com.barnizgallery.backend.dto.ReportResponse;
import com.barnizgallery.backend.service.ContentReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Reports of content by logged-in users.
 */
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Conversations")
public class ReportController {

    private final ContentReportService reportService;

    public ReportController(ContentReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Report an artwork, store, message, custom request or user to the administrators")
    public ReportResponse report(@Valid @RequestBody ReportRequest request) {
        return reportService.report(request);
    }
}
