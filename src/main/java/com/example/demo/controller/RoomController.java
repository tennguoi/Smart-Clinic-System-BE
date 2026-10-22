package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.AssignRoomRequest;
import com.example.demo.dto.QueuePatientResponse;
import com.example.demo.dto.RoomResponse;
import com.example.demo.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reception/rooms")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TIEP_TAN', 'BAC_SI', 'ADMIN')")
public class RoomController {

    private final RoomService roomService;

    /**
     * Lấy tất cả phòng khám đang hoạt động
     * GET /api/rooms
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAllRooms() {
        List<RoomResponse> rooms = roomService.getAllActiveRooms();
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    /**
     * Lấy danh sách phòng trống (Available)
     * GET /api/rooms/available
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAvailableRooms() {
        List<RoomResponse> rooms = roomService.getAvailableRooms();
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    /**
     * Lấy chi tiết một phòng khám
     * GET /api/rooms/{roomId}
     * ⚠️ Đặt SAU các route cụ thể như /assign, /available
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(@PathVariable UUID roomId) {
        RoomResponse room = roomService.getRoomById(roomId);
        return ResponseEntity.ok(ApiResponse.success(room));
    }

    /**
     * HOÀN TẤT KHÁM - Giải phóng phòng
     * POST /api/rooms/{roomId}/complete
     */
    @PostMapping("/{roomId}/complete")
    public ResponseEntity<ApiResponse<RoomResponse>> completeExamination(
            @PathVariable UUID roomId) {
        ApiResponse<RoomResponse> response = roomService.completeExamination(roomId);
        return ResponseEntity.ok(response);
    }

    /**
     * HỦY PHÂN PHÒNG - Đưa bệnh nhân về hàng đợi
     * POST /api/rooms/{roomId}/cancel
     */
    @PostMapping("/{roomId}/cancel")
    public ResponseEntity<ApiResponse<RoomResponse>> cancelRoomAssignment(
            @PathVariable UUID roomId) {
        ApiResponse<RoomResponse> response = roomService.cancelRoomAssignment(roomId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy thông tin bệnh nhân hiện tại trong phòng
     * GET /api/rooms/{roomId}/current-patient
     */
    @GetMapping("/{roomId}/current-patient")
    public ResponseEntity<ApiResponse<QueuePatientResponse>> getCurrentPatient(
            @PathVariable UUID roomId) {
        QueuePatientResponse patient = roomService.getCurrentPatientInRoom(roomId);
        if (patient == null) {
            return ResponseEntity.ok(ApiResponse.success("Phòng không có bệnh nhân", null));
        }
        return ResponseEntity.ok(ApiResponse.success(patient));
    }

    /**
     * Lấy lịch sử khám trong phòng (hôm nay)
     * GET /api/rooms/{roomId}/history
     */
    @GetMapping("/{roomId}/history")
    public ResponseEntity<ApiResponse<List<QueuePatientResponse>>> getRoomHistory(
            @PathVariable UUID roomId) {
        List<QueuePatientResponse> history = roomService.getRoomHistory(roomId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    /**
     * Thống kê phòng khám
     * GET /api/rooms/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<RoomService.RoomStatistics>> getStatistics() {
        RoomService.RoomStatistics stats = roomService.getRoomStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    @PostMapping("/{roomId}/call-next")
    public ResponseEntity<ApiResponse<QueuePatientResponse>> callNextPatient(@PathVariable UUID roomId) {
        var result = roomService.callNextPatient(roomId);
        return ResponseEntity.ok(result);
    }
}