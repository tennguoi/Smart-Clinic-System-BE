package com.example.demo.service;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.AssignRoomRequest;
import com.example.demo.dto.QueuePatientResponse;
import com.example.demo.dto.RoomResponse;
import com.example.demo.entity.ExaminationRoom;
import com.example.demo.entity.Queue;
import com.example.demo.entity.User;
import com.example.demo.repository.ExaminationRoomRepository;
import com.example.demo.repository.QueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final ExaminationRoomRepository roomRepository;
    private final QueueRepository queueRepository;
    private final QueueService queueService;

    /**
     * Lấy danh sách tất cả phòng khám đang hoạt động
     */
    public List<RoomResponse> getAllActiveRooms() {
        List<ExaminationRoom> rooms = roomRepository.findByIsActiveTrueOrderByRoomNameAsc();
        return rooms.stream()
                .map(this::mapToRoomResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách phòng trống (Available)
     */
    public List<RoomResponse> getAvailableRooms() {
        List<ExaminationRoom> rooms = roomRepository.findByStatus(ExaminationRoom.RoomStatus.Available);
        return rooms.stream()
                .filter(room -> room.getIsActive())
                .map(this::mapToRoomResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chi tiết một phòng khám
     */
    public RoomResponse getRoomById(UUID roomId) {
        ExaminationRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng khám"));
        return mapToRoomResponse(room);
    }

    /**
     * PHÂN BỆNH NHÂN VÀO PHÒNG KHÁM
     * Nghiệp vụ chính: Lễ tân chọn phòng trống và phân bệnh nhân đang chờ vào phòng đó
     */
    @Transactional
    public ApiResponse<RoomResponse> assignPatientToRoom(AssignRoomRequest request) {
        // 1. Kiểm tra phòng khám có tồn tại và đang trống không
        ExaminationRoom room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng khám"));

        if (!room.getIsActive()) {
            return ApiResponse.error("Phòng khám đang không hoạt động");
        }

        if (room.getStatus() == ExaminationRoom.RoomStatus.Occupied) {
            return ApiResponse.error("Phòng khám đang bận, vui lòng chọn phòng khác");
        }

        // 2. Kiểm tra bệnh nhân trong hàng đợi
        Queue queue = queueRepository.findById(request.getQueueId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân trong hàng đợi"));

        if (queue.getStatus() != Queue.QueueStatus.Waiting) {
            return ApiResponse.error("Bệnh nhân không ở trạng thái chờ khám");
        }

        // 3. Cập nhật QUEUE
        queue.setAssignedRoom(room);
        queue.setStatus(Queue.QueueStatus.InProgress);
        queue.setStartTime(LocalDateTime.now());
        queueRepository.save(queue);

        // 4. Cập nhật EXAMINATION_ROOM
        room.setCurrentQueueId(queue.getQueueId());
        room.setStatus(ExaminationRoom.RoomStatus.Occupied);
        ExaminationRoom savedRoom = roomRepository.save(room);

        return ApiResponse.success(
                "Đã phân bệnh nhân " + queue.getQueueNumber() + " vào phòng " + room.getRoomName(),
                mapToRoomResponse(savedRoom)
        );
    }

    /**
     * HOÀN TẤT KHÁM - Giải phóng phòng
     * Khi bác sĩ khám xong, đánh dấu hoàn tất và phòng trở về trạng thái Available
     */
    @Transactional
    public ApiResponse<RoomResponse> completeExamination(UUID roomId) {
        ExaminationRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng khám"));

        if (room.getCurrentQueueId() == null) {
            return ApiResponse.error("Phòng không có bệnh nhân nào");
        }

        // Cập nhật Queue: đánh dấu hoàn tất
        Queue queue = queueRepository.findById(room.getCurrentQueueId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khám"));

        queue.setStatus(Queue.QueueStatus.Completed);
        queue.setEndTime(LocalDateTime.now());
        queueRepository.save(queue);

        // Giải phóng phòng
        room.setCurrentQueueId(null);
        room.setStatus(ExaminationRoom.RoomStatus.Available);
        ExaminationRoom savedRoom = roomRepository.save(room);

        return ApiResponse.success(
                "Đã hoàn tất khám cho bệnh nhân " + queue.getQueueNumber(),
                mapToRoomResponse(savedRoom)
        );
    }

    /**
     * HUỶ PHÂN PHÒNG - Đưa bệnh nhân về hàng đợi
     */
    @Transactional
    public ApiResponse<RoomResponse> cancelRoomAssignment(UUID roomId) {
        ExaminationRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng khám"));

        if (room.getCurrentQueueId() == null) {
            return ApiResponse.error("Phòng không có bệnh nhân nào");
        }

        // Đưa bệnh nhân về trạng thái Waiting
        Queue queue = queueRepository.findById(room.getCurrentQueueId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khám"));

        queue.setAssignedRoom(null);
        queue.setStatus(Queue.QueueStatus.Waiting);
        queue.setStartTime(null);
        queueRepository.save(queue);

        // Giải phóng phòng
        room.setCurrentQueueId(null);
        room.setStatus(ExaminationRoom.RoomStatus.Available);
        ExaminationRoom savedRoom = roomRepository.save(room);

        return ApiResponse.success(
                "Đã hủy phân phòng cho bệnh nhân " + queue.getQueueNumber(),
                mapToRoomResponse(savedRoom)
        );
    }

    /**
     * Lấy thông tin bệnh nhân hiện tại trong phòng
     */
    public QueuePatientResponse getCurrentPatientInRoom(UUID roomId) {
        ExaminationRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng khám"));

        if (room.getCurrentQueueId() == null) {
            return null;
        }

        return queueService.getQueueById(room.getCurrentQueueId())
                .orElse(null);
    }

    /**
     * Lấy danh sách bệnh nhân đã khám trong phòng (hôm nay)
     */
    public List<QueuePatientResponse> getRoomHistory(UUID roomId) {
        List<Queue> queues = queueRepository.findByAssignedRoomRoomId(roomId);
        return queues.stream()
                .filter(q -> q.getStatus() == Queue.QueueStatus.Completed)
                .map(queueService::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Thống kê phòng khám
     */
    public RoomStatistics getRoomStatistics() {
        List<ExaminationRoom> allRooms = roomRepository.findByIsActiveTrue();
        long availableCount = allRooms.stream()
                .filter(r -> r.getStatus() == ExaminationRoom.RoomStatus.Available)
                .count();
        long occupiedCount = allRooms.stream()
                .filter(r -> r.getStatus() == ExaminationRoom.RoomStatus.Occupied)
                .count();

        return RoomStatistics.builder()
                .totalRooms(allRooms.size())
                .availableRooms((int) availableCount)
                .occupiedRooms((int) occupiedCount)
                .build();
    }

    // === HELPER METHODS ===

    private RoomResponse mapToRoomResponse(ExaminationRoom room) {
        RoomResponse response = new RoomResponse();
        response.setRoomId(room.getRoomId());
        response.setRoomName(room.getRoomName());
        response.setStatus(room.getStatus().name());
        response.setIsActive(room.getIsActive());
        response.setCurrentQueueId(room.getCurrentQueueId());

        if (room.getDoctor() != null) {
            User doctor = room.getDoctor();
            response.setDoctorId(doctor.getUserId());
            response.setDoctorName(doctor.getFullName());
        }

        return response;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RoomStatistics {
        private int totalRooms;
        private int availableRooms;
        private int occupiedRooms;
    }
}
