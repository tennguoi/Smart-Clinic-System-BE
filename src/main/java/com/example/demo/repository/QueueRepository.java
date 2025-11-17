package com.example.demo.repository;

import com.example.demo.entity.Queue;
import com.example.demo.entity.Queue.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QueueRepository extends JpaRepository<Queue, UUID> {

    List<Queue> findByStatusOrderByCheckInTimeAsc(QueueStatus status);
    Queue findTopByOrderByCreatedAtDesc();
    // Tìm tất cả queue đang chờ, sắp xếp theo priority và checkInTime
    // Dùng cho lễ tân xem danh sách bệnh nhân đang chờ (bao gồm cả đã phân phòng và chưa phân phòng)
    @Query("SELECT q FROM Queue q " +
            "LEFT JOIN FETCH q.patient " +
            "LEFT JOIN FETCH q.assignedRoom " +
            "WHERE q.status = 'Waiting' " +
            "ORDER BY CASE q.priority " +
            "WHEN 'Emergency' THEN 1 " +
            "WHEN 'Urgent' THEN 2 " +
            "WHEN 'Normal' THEN 3 END, " +
            "q.checkInTime ASC")
    List<Queue> findWaitingQueuesOrderedByPriority();
    
    // Tìm queue đang chờ và chưa được phân phòng (assignedRoom = null)
    // Dùng cho lễ tân khi cần phân phòng
    @Query("SELECT q FROM Queue q " +
            "LEFT JOIN FETCH q.patient " +
            "WHERE q.status = 'Waiting' AND q.assignedRoom IS NULL " +
            "ORDER BY CASE q.priority " +
            "WHEN 'Emergency' THEN 1 " +
            "WHEN 'Urgent' THEN 2 " +
            "WHEN 'Normal' THEN 3 END, " +
            "q.checkInTime ASC")
    List<Queue> findUnassignedWaitingQueuesOrderedByPriority();

    // Đếm số bệnh nhân đang chờ
    long countByStatus(Queue.QueueStatus status);

    // Tìm queue theo phòng khám
    List<Queue> findByAssignedRoomRoomId(UUID roomId);

    // Tìm queue theo phòng khám và status
    List<Queue> findByAssignedRoomRoomIdAndStatus(UUID roomId, QueueStatus status);

    // Tìm queue đang chờ trong phòng, sắp xếp theo priority và checkInTime
    // Đảm bảo chỉ lấy bệnh nhân đã được phân vào phòng (có assignedRoom) và status = Waiting
    @Query("SELECT q FROM Queue q " +
            "LEFT JOIN FETCH q.patient " +
            "LEFT JOIN FETCH q.assignedRoom " +
            "WHERE q.assignedRoom.roomId = :roomId AND q.status = 'Waiting' " +
            "ORDER BY CASE q.priority " +
            "WHEN 'Emergency' THEN 1 " +
            "WHEN 'Urgent' THEN 2 " +
            "WHEN 'Normal' THEN 3 END, " +
            "q.checkInTime ASC")
    List<Queue> findWaitingQueuesByRoomIdOrderedByPriority(UUID roomId);

    // Tìm tất cả queue đã được phân vào phòng (bao gồm cả InProgress và Waiting), sắp xếp theo priority và checkInTime
    // Đảm bảo chỉ lấy bệnh nhân đã được phân vào phòng (có assignedRoom)
    @Query("SELECT q FROM Queue q " +
            "LEFT JOIN FETCH q.patient " +
            "LEFT JOIN FETCH q.assignedRoom " +
            "WHERE q.assignedRoom.roomId = :roomId AND q.status IN ('Waiting', 'InProgress') " +
            "ORDER BY CASE q.priority " +
            "WHEN 'Emergency' THEN 1 " +
            "WHEN 'Urgent' THEN 2 " +
            "WHEN 'Normal' THEN 3 END, " +
            "q.checkInTime ASC")
    List<Queue> findAssignedQueuesByRoomIdOrderedByPriority(UUID roomId);

    // Tìm queue theo ID với eager fetch patient và assignedRoom (dùng cho getCurrentPatient)
    @Query("SELECT q FROM Queue q " +
            "LEFT JOIN FETCH q.patient " +
            "LEFT JOIN FETCH q.assignedRoom " +
            "WHERE q.queueId = :queueId")
    Optional<Queue> findByIdWithPatientAndRoom(UUID queueId);

}
