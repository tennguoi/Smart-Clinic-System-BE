package com.example.demo.controller;

import com.example.demo.dto.DoctorSimpleResponse;
import com.example.demo.dto.ExaminationRoomRequest;
import com.example.demo.dto.ExaminationRoomResponse;
import com.example.demo.service.ExaminationRoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/receptionist/examination-rooms")
public class ReceptionistExaminationRoomController {

    @Autowired
    private ExaminationRoomService roomService;

    @GetMapping
    public ResponseEntity<List<ExaminationRoomResponse>> getAllRooms(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
        
        List<ExaminationRoomResponse> rooms;
        
        if (keyword != null && !keyword.isEmpty()) {
            rooms = roomService.searchRooms(keyword);
        } else if (status != null && !status.isEmpty()) {
            rooms = roomService.getRoomsByStatus(status);
        } else if (activeOnly) {
            rooms = roomService.getActiveRooms();
        } else {
            rooms = roomService.getAllRooms();
        }
        
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ExaminationRoomResponse> getRoomById(@PathVariable UUID roomId) {
        return ResponseEntity.ok(roomService.getRoomById(roomId));
    }

    @PostMapping
    public ResponseEntity<ExaminationRoomResponse> createRoom(@Valid @RequestBody ExaminationRoomRequest request) {
        try {
            ExaminationRoomResponse response = roomService.createRoom(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<ExaminationRoomResponse> updateRoom(
            @PathVariable UUID roomId,
            @Valid @RequestBody ExaminationRoomRequest request) {
        try {
            ExaminationRoomResponse response = roomService.updateRoom(roomId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable UUID roomId) {
        try {
            roomService.deleteRoom(roomId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Lấy danh sách bác sĩ chưa có phòng active để chọn khi tạo phòng mới
     */
    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorSimpleResponse>> getAllDoctors() {
        return ResponseEntity.ok(roomService.getAllDoctors());
    }

    /**
     * Lấy danh sách bác sĩ để chọn khi cập nhật phòng
     * Bao gồm: bác sĩ chưa có phòng + bác sĩ hiện tại của phòng đang được cập nhật
     * @param roomId ID của phòng đang được cập nhật (optional, nếu null thì giống getAllDoctors)
     */
    @GetMapping("/doctors/for-update")
    public ResponseEntity<List<DoctorSimpleResponse>> getDoctorsForUpdate(
            @RequestParam(required = false) UUID roomId) {
        return ResponseEntity.ok(roomService.getAvailableDoctorsForUpdate(roomId));
    }

    /**
     * Lấy danh sách phòng theo bác sĩ
     */
    @GetMapping("/by-doctor/{doctorId}")
    public ResponseEntity<List<ExaminationRoomResponse>> getRoomsByDoctor(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(roomService.getRoomsByDoctor(doctorId));
    }

    /**
     * Thống kê: Mỗi bác sĩ đang phụ trách bao nhiêu phòng
     */
    @GetMapping("/statistics/room-count-by-doctor")
    public ResponseEntity<Map<String, Long>> getRoomCountByDoctor() {
        return ResponseEntity.ok(roomService.getRoomCountByDoctor());
    }
}

