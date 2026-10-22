package com.example.demo.controller.reception;

import com.example.demo.dto.AssignRoomRequest;
import com.example.demo.dto.QueuePatientResponse;
import com.example.demo.dto.doctor.DoctorSimpleResponse;
import com.example.demo.dto.ExaminationRoomRequest;
import com.example.demo.dto.ExaminationRoomResponse;
import com.example.demo.service.ExaminationRoomService;
import com.example.demo.service.QueueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/receptionist/examination-rooms")
@PreAuthorize("hasAnyRole('ADMIN', 'TIEP_TAN')")
public class ReceptionistExaminationRoomController {

    @Autowired
    private ExaminationRoomService roomService;
    @Autowired
    private QueueService queueService;

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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExaminationRoomResponse> createRoom(@Valid @RequestBody ExaminationRoomRequest request) {
        try {
            ExaminationRoomResponse response = roomService.createRoom(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable UUID roomId) {
        try {
            roomService.deleteRoom(roomId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorSimpleResponse>> getAllDoctors() {
        return ResponseEntity.ok(roomService.getAllDoctors());
    }

    @GetMapping("/doctors/for-update")
    public ResponseEntity<List<DoctorSimpleResponse>> getDoctorsForUpdate(
            @RequestParam(required = false) UUID roomId) {
        return ResponseEntity.ok(roomService.getAvailableDoctorsForUpdate(roomId));
    }

    @GetMapping("/by-doctor/{doctorId}")
    public ResponseEntity<List<ExaminationRoomResponse>> getRoomsByDoctor(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(roomService.getRoomsByDoctor(doctorId));
    }

    @GetMapping("/statistics/room-count-by-doctor")
    public ResponseEntity<Map<String, Long>> getRoomCountByDoctor() {
        return ResponseEntity.ok(roomService.getRoomCountByDoctor());
    }

    /**
     * Lấy danh sách phòng sẵn sàng để phân phòng
     * Chỉ trả về phòng có bác sĩ và status = Available
     */
    @GetMapping("/available-for-assignment")
    public ResponseEntity<List<ExaminationRoomResponse>> getAvailableRoomsForAssignment() {
        return ResponseEntity.ok(roomService.getAvailableRoomsForAssignment());
    }

    /**
     * Lấy phòng của một bác sĩ cụ thể (nếu có)
     * Dùng để đề xuất phòng mặc định
     */
    @GetMapping("/by-doctor-id/{doctorId}")
    public ResponseEntity<ExaminationRoomResponse> getRoomByDoctorId(@PathVariable UUID doctorId) {
        return roomService.getRoomByDoctorId(doctorId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/assign")
    public ResponseEntity<?> assignPatientToRoom(
            @Valid @RequestBody AssignRoomRequest request) {
        try {
            ExaminationRoomResponse response = roomService.assignPatientToRoom(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Trả về error message cụ thể để frontend hiển thị
            return ResponseEntity.badRequest()
                    .body(new java.util.HashMap<String, String>() {
                        {
                            put("error", e.getMessage());
                        }
                    });
        }
    }

    @GetMapping("/waiting")
    public ResponseEntity<?> getWaitingPatients(
            @RequestParam(required = false) UUID roomId) { // Thêm filter theo phòng nếu cần
        List<QueuePatientResponse> list = roomId != null
                ? queueService.getWaitingPatientsByRoom(roomId)
                : queueService.getAllWaitingPatients(); // hoặc chỉ trả về của các phòng active
        return ResponseEntity.ok(list);
    }
}
