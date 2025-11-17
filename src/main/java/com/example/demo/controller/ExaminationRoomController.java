package com.example.demo.controller;

import com.example.demo.dto.ExaminationRoomResponse;
import com.example.demo.service.ExaminationRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/examination-rooms")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TIEP_TAN', 'ADMIN', 'BAC_SI')")
public class ExaminationRoomController {

    private final ExaminationRoomService examinationRoomService;

    @GetMapping
    public ResponseEntity<List<ExaminationRoomResponse>> getAllRooms() {
        List<ExaminationRoomResponse> rooms = examinationRoomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/available")
    public ResponseEntity<List<ExaminationRoomResponse>> getAvailableRooms() {
        List<ExaminationRoomResponse> rooms = examinationRoomService.getAvailableRooms();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ExaminationRoomResponse> getRoomById(@PathVariable UUID roomId) {
        try {
            ExaminationRoomResponse room = examinationRoomService.getRoomById(roomId);
            return ResponseEntity.ok(room);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

