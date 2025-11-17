package com.example.demo.repository;

import com.example.demo.entity.Queue;
import com.example.demo.entity.Queue.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface QueueRepository extends JpaRepository<Queue, UUID> {

    List<Queue> findByStatusOrderByCheckInTimeAsc(QueueStatus status);
    Queue findTopByOrderByCreatedAtDesc();
    @Query("SELECT q FROM Queue q WHERE q.status = 'Waiting' " +
            "ORDER BY CASE q.priority " +
            "WHEN 'Emergency' THEN 1 " +
            "WHEN 'Urgent' THEN 2 " +
            "WHEN 'Normal' THEN 3 END, " +
            "q.checkInTime ASC")
    List<Queue> findWaitingQueuesOrderedByPriority();

    // Đếm số bệnh nhân đang chờ
    long countByStatus(Queue.QueueStatus status);

    // Tìm queue theo phòng khám
    List<Queue> findByAssignedRoomRoomId(UUID roomId);

}
