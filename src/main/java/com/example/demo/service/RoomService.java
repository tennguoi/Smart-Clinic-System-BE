package com.example.demo.service;

import com.example.demo.dto.*;
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

@Service
@RequiredArgsConstructor
public class RoomService {

    private final ExaminationRoomRepository roomRepository;
    private final QueueRepository queueRepository;
    private final QueueService queueService;

    public List<RoomResponse> getAllActiveRooms() {
        List<ExaminationRoom> rooms = roomRepository.findByIsActiveTrueOrderByRoomNameAsc();
        return rooms.stream()
                .map(this::mapToRoomResponse)
                .toList();
    }

    public List<RoomResponse> getAvailableRooms() {
        return roomRepository.findByIsActiveTrue()
                .stream()
                .filter(r -> r.getDoctor() != null)
                .map(this::mapToRoomResponse)
                .toList();
    }

    public RoomResponse getRoomById(UUID roomId) {
        ExaminationRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng khám"));
        return mapToRoomResponse(room);
    }

    @Transactional
    public ApiResponse<RoomResponse> assignPatientToRoom(AssignRoomRequest request) {
        ExaminationRoom room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng khám"));

        if (!room.getIsActive()) {
            return ApiResponse.error("Phòng khám đang không hoạt động");
        }

        Queue queue = queueRepository.findById(request.getQueueId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân"));

        if (queue.getStatus() != Queue.QueueStatus.Waiting) {
            return ApiResponse.error("Bệnh nhân không ở trạng thái chờ khám");
        }

        queue.setAssignedRoom(room);
        queueRepository.save(queue);

        return ApiResponse.success("Đã phân bệnh nhân vào phòng", mapToRoomResponse(room));
    }

    @Transactional
    public ApiResponse<QueuePatientResponse> callNextPatient(UUID roomId) {
        ExaminationRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng khám"));

        if (!room.getIsActive() || room.getDoctor() == null) {
            return ApiResponse.error("Phòng khám không hoạt động hoặc chưa có bác sĩ");
        }

        List<Queue> waiting = queueRepository.findWaitingQueuesByRoomIdOrderedByPriority(roomId);

        if (waiting.isEmpty()) {
            return ApiResponse.error("Không còn bệnh nhân nào chờ trong phòng");
        }

        Queue next = waiting.get(0);

        // Hoàn tất người đang khám (nếu có)
        if (room.getCurrentQueueId() != null) {
            queueRepository.findById(room.getCurrentQueueId())
                    .ifPresent(current -> {
                        current.setStatus(Queue.QueueStatus.Completed);
                        current.setEndTime(LocalDateTime.now());
                        queueRepository.save(current);
                    });
        }

        // Gọi bệnh mới
        next.setStatus(Queue.QueueStatus.InProgress);
        next.setStartTime(LocalDateTime.now());
        queueRepository.save(next);

        room.setCurrentQueueId(next.getQueueId());
        room.setStatus(ExaminationRoom.RoomStatus.Occupied);
        roomRepository.save(room);

        return ApiResponse.success("Đã gọi bệnh nhân: " + next.getQueueNumber(), queueService.mapToDto(next));
    }

    @Transactional
    public ApiResponse<RoomResponse> completeExamination(UUID roomId) {
        ExaminationRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng khám"));

        if (room.getCurrentQueueId() == null) {
            return ApiResponse.error("Phòng không có bệnh nhân đang khám");
        }

        Queue current = queueRepository.findById(room.getCurrentQueueId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân"));

        current.setStatus(Queue.QueueStatus.Completed);
        current.setEndTime(LocalDateTime.now());
        queueRepository.save(current);

        room.setCurrentQueueId(null);
        room.setStatus(ExaminationRoom.RoomStatus.Available);
        roomRepository.save(room);

        return ApiResponse.success("Hoàn tất khám", mapToRoomResponse(room));
    }

    @Transactional
    public ApiResponse<RoomResponse> cancelRoomAssignment(UUID roomId) {
        ExaminationRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng khám"));

        if (room.getCurrentQueueId() == null) {
            return ApiResponse.error("Phòng không có bệnh nhân nào");
        }

        Queue queue = queueRepository.findById(room.getCurrentQueueId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khám"));

        queue.setAssignedRoom(null);
        queue.setStatus(Queue.QueueStatus.Waiting);
        queue.setStartTime(null);
        queueRepository.save(queue);

        room.setCurrentQueueId(null);
        room.setStatus(ExaminationRoom.RoomStatus.Available);
        roomRepository.save(room);

        return ApiResponse.success("Đã hủy phân phòng", mapToRoomResponse(room));
    }

    public QueuePatientResponse getCurrentPatientInRoom(UUID roomId) {
        ExaminationRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng khám"));

        if (room.getCurrentQueueId() == null) return null;

        return queueRepository.findById(room.getCurrentQueueId())
                .map(queueService::mapToDto)
                .orElse(null);
    }

    public List<QueuePatientResponse> getRoomHistory(UUID roomId) {
        return queueRepository.findByAssignedRoomRoomId(roomId)
                .stream()
                .filter(q -> q.getStatus() == Queue.QueueStatus.Completed)
                .map(queueService::mapToDto)
                .toList();
    }

    public RoomStatistics getRoomStatistics() {
        List<ExaminationRoom> allRooms = roomRepository.findByIsActiveTrue();
        long available = allRooms.stream()
                .filter(r -> r.getStatus() == ExaminationRoom.RoomStatus.Available)
                .count();
        long occupied = allRooms.size() - available;

        return RoomStatistics.builder()
                .totalRooms((int) allRooms.size())
                .availableRooms((int) available)
                .occupiedRooms((int) occupied)
                .build();
    }

    private RoomResponse mapToRoomResponse(ExaminationRoom room) {
        RoomResponse res = new RoomResponse();
        res.setRoomId(room.getRoomId());
        res.setRoomName(room.getRoomName());
        res.setStatus(room.getStatus().name());
        res.setIsActive(room.getIsActive());
        res.setCurrentQueueId(room.getCurrentQueueId());

        if (room.getDoctor() != null) {
            res.setDoctorId(room.getDoctor().getUserId());
            res.setDoctorName(room.getDoctor().getFullName());
        }
        return res;
    }

    @lombok.Data
    @lombok.Builder
    public static class RoomStatistics {
        private int totalRooms;
        private int availableRooms;
        private int occupiedRooms;
    }
}