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

import com.barnizgallery.backend.dto.request.RoomRequest;
import com.barnizgallery.backend.dto.response.RoomDetailResponse;
import com.barnizgallery.backend.dto.response.RoomSummaryResponse;
import com.barnizgallery.backend.service.RoomService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Rooms of the gallery (one per master). There is no DELETE endpoint on purpose.
 */
@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<RoomSummaryResponse> findAll() {
        return roomService.findAll();
    }

    @GetMapping("/{id}")
    public RoomDetailResponse findById(@PathVariable Integer id) {
        return roomService.findById(id);
    }

    @PostMapping
    public ResponseEntity<RoomSummaryResponse> create(@Valid @RequestBody RoomRequest request) {
        RoomSummaryResponse created = roomService.create(request);
        return ResponseEntity.created(URI.create("/api/rooms/" + created.roomId())).body(created);
    }

    @PutMapping("/{id}")
    public RoomSummaryResponse update(@PathVariable Integer id, @Valid @RequestBody RoomRequest request) {
        return roomService.update(id, request);
    }
}
