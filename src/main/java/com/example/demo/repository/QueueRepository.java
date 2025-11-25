package com.example.demo.repository;

import com.example.demo.entity.ExaminationRoom;
import com.example.demo.entity.Queue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QueueRepository extends JpaRepository<Queue, UUID> {

    // ============================= CƠ BẢN =============================
    Queue findTopByOrderByCreatedAtDesc();
    List<Queue> findByStatusOrderByCheckInTimeAsc(Queue.QueueStatus status);
    List<Queue> findByStatusOrderByEndTimeDesc(Queue.QueueStatus status);

    // ============================= LỄ TÂN: XEM TẤT CẢ =============================
    @Query("""
            SELECT q FROM Queue q
            LEFT JOIN FETCH q.patient p
            LEFT JOIN FETCH q.assignedRoom r
            LEFT JOIN FETCH r.doctor
            WHERE q.status IN (:waiting, :inProgress)
            ORDER BY
                CASE q.priority
                    WHEN :emergency THEN 1
                    WHEN :urgent THEN 2
                    WHEN :normal THEN 3
                END,
                q.checkInTime ASC
            """)
    List<Queue> findAllActiveQueuesOrderedByPriority(
            @Param("waiting") Queue.QueueStatus waiting,
            @Param("inProgress") Queue.QueueStatus inProgress,
            @Param("emergency") Queue.Priority emergency,
            @Param("urgent") Queue.Priority urgent,
            @Param("normal") Queue.Priority normal
    );

    // ============================= BÁC SĨ: XEM DANH SÁCH CHỜ TRONG PHÒNG =============================
    @Query("""
            SELECT q FROM Queue q
            LEFT JOIN FETCH q.patient
            WHERE q.assignedRoom.roomId = :roomId
              AND q.status = :waiting
            ORDER BY
                CASE q.priority
                    WHEN :emergency THEN 1
                    WHEN :urgent THEN 2
                    WHEN :normal THEN 3
                END,
                q.checkInTime ASC
            """)
    List<Queue> findWaitingInRoom(
            @Param("roomId") UUID roomId,
            @Param("waiting") Queue.QueueStatus waiting,
            @Param("emergency") Queue.Priority emergency,
            @Param("urgent") Queue.Priority urgent,
            @Param("normal") Queue.Priority normal
    );

    // Lấy cả Waiting + InProgress trong phòng
    @Query("""
            SELECT q FROM Queue q
            LEFT JOIN FETCH q.patient
            WHERE q.assignedRoom.roomId = :roomId
              AND q.status IN (:waiting, :inProgress)
            ORDER BY q.status DESC, q.checkInTime ASC
            """)
    List<Queue> findActiveInRoom(
            @Param("roomId") UUID roomId,
            @Param("waiting") Queue.QueueStatus waiting,
            @Param("inProgress") Queue.QueueStatus inProgress
    );

    // ============================= ĐẾM SỐ NGƯỜI CHỜ TRONG PHÒNG =============================
    @Query("SELECT COUNT(q) FROM Queue q WHERE q.assignedRoom = :room AND q.status = :waiting")
    long countWaitingPatientsInRoom(
            @Param("room") ExaminationRoom room,
            @Param("waiting") Queue.QueueStatus waiting
    );

    // ============================= LẤY CHI TIẾT QUEUE FULL =============================
    @Query("SELECT q FROM Queue q LEFT JOIN FETCH q.patient LEFT JOIN FETCH q.assignedRoom r LEFT JOIN FETCH r.doctor WHERE q.queueId = :id")
    Optional<Queue> findByIdWithAllRelations(@Param("id") UUID id);

    // ============================= HOÀN THÀNH =============================
    List<Queue> findByStatus(Queue.QueueStatus status);

    @Query("SELECT q FROM Queue q LEFT JOIN FETCH q.patient WHERE q.status = :completed ORDER BY q.endTime DESC")
    List<Queue> findCompletedQueues(@Param("completed") Queue.QueueStatus completed);

    // ============================= CÁC METHOD CŨ (GIỮ LẠI ĐỂ KHÔNG LỖI) =============================
    @Query("""
            SELECT q FROM Queue q
            LEFT JOIN FETCH q.patient
            LEFT JOIN FETCH q.assignedRoom
            WHERE q.status = com.example.demo.entity.Queue.QueueStatus.Waiting
            ORDER BY CASE q.priority
                WHEN com.example.demo.entity.Queue.Priority.Emergency THEN 1
                WHEN com.example.demo.entity.Queue.Priority.Urgent THEN 2
                WHEN com.example.demo.entity.Queue.Priority.Normal THEN 3
            END,
            q.checkInTime ASC
            """)
    List<Queue> findWaitingQueuesOrderedByPriority();

    @Query("""
            SELECT q FROM Queue q
            LEFT JOIN FETCH q.patient
            WHERE q.status = com.example.demo.entity.Queue.QueueStatus.Waiting
              AND q.assignedRoom IS NULL
            ORDER BY CASE q.priority
                WHEN com.example.demo.entity.Queue.Priority.Emergency THEN 1
                WHEN com.example.demo.entity.Queue.Priority.Urgent THEN 2
                WHEN com.example.demo.entity.Queue.Priority.Normal THEN 3
            END,
            q.checkInTime ASC
            """)
    List<Queue> findUnassignedWaitingQueuesOrderedByPriority();

    List<Queue> findByAssignedRoomRoomId(UUID roomId);

    @Query("""
            SELECT q FROM Queue q
            LEFT JOIN FETCH q.patient
            LEFT JOIN FETCH q.assignedRoom
            WHERE q.assignedRoom.roomId = :roomId
              AND q.status = com.example.demo.entity.Queue.QueueStatus.Waiting
            ORDER BY CASE q.priority
                WHEN com.example.demo.entity.Queue.Priority.Emergency THEN 1
                WHEN com.example.demo.entity.Queue.Priority.Urgent THEN 2
                WHEN com.example.demo.entity.Queue.Priority.Normal THEN 3
            END,
            q.checkInTime ASC
            """)
    List<Queue> findWaitingQueuesByRoomIdOrderedByPriority(@Param("roomId") UUID roomId);

    @Query("""
            SELECT q FROM Queue q
            LEFT JOIN FETCH q.patient
            LEFT JOIN FETCH q.assignedRoom
            WHERE q.assignedRoom.roomId = :roomId
              AND q.status IN (com.example.demo.entity.Queue.QueueStatus.Waiting,
                               com.example.demo.entity.Queue.QueueStatus.InProgress)
            ORDER BY CASE q.priority
                WHEN com.example.demo.entity.Queue.Priority.Emergency THEN 1
                WHEN com.example.demo.entity.Queue.Priority.Urgent THEN 2
                WHEN com.example.demo.entity.Queue.Priority.Normal THEN 3
            END,
            q.checkInTime ASC
            """)
    List<Queue> findAssignedQueuesByRoomIdOrderedByPriority(@Param("roomId") UUID roomId);

    @Query("""
            SELECT q FROM Queue q
            LEFT JOIN FETCH q.patient
            LEFT JOIN FETCH q.assignedRoom
            WHERE q.queueId = :queueId
            """)
    Optional<Queue> findByIdWithPatientAndRoom(@Param("queueId") UUID queueId);

    @Query("SELECT COUNT(q) FROM Queue q WHERE q.assignedRoom = :room AND q.status IN :statuses")
    long countByAssignedRoomAndStatusIn(
            @Param("room") ExaminationRoom room,
            @Param("statuses") List<Queue.QueueStatus> statuses
    );

    // ============================= THỐNG KÊ BÁC SĨ =============================
    @Query("""
            SELECT q FROM Queue q
            JOIN q.assignedRoom r
            JOIN r.doctor d
            LEFT JOIN FETCH q.patient
            LEFT JOIN FETCH q.assignedRoom
            WHERE d.userId = :doctorId
              AND q.checkInTime >= :start
              AND q.checkInTime < :end
              AND (q.status IS NULL OR q.status <> :excludedStatus)
            """)
    List<Queue> findDoctorQueuesWithinRange(
            @Param("doctorId") UUID doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("excludedStatus") Queue.QueueStatus excludedStatus
    );

    @Query(value = """
            SELECT q FROM Queue q
            JOIN q.assignedRoom r
            JOIN r.doctor d
            LEFT JOIN FETCH q.patient
            LEFT JOIN FETCH q.assignedRoom
            WHERE d.userId = :doctorId
              AND q.checkInTime >= :start
              AND q.checkInTime < :end
              AND (q.status IS NULL OR q.status <> :excludedStatus)
            """,
            countQuery = """
            SELECT COUNT(q) FROM Queue q
            JOIN q.assignedRoom r
            JOIN r.doctor d
            WHERE d.userId = :doctorId
              AND q.checkInTime >= :start
              AND q.checkInTime < :end
              AND (q.status IS NULL OR q.status <> :excludedStatus)
            """)
    Page<Queue> findDoctorQueuesWithinRange(
            @Param("doctorId") UUID doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("excludedStatus") Queue.QueueStatus excludedStatus,
            Pageable pageable
    );

    long countByStatus(Queue.QueueStatus status);

    // ============================= ✅ SỬA LỖI: THÊM METHOD ĐÚNG TÊN =============================

    /**
     * Tìm Queue theo roomId và status, sắp xếp theo priority và checkInTime
     * SỬ DỤNG: findByAssignedRoom_RoomIdAndStatusOrderByPriorityAscCheckInTimeAsc
     */
    List<Queue> findByAssignedRoom_RoomIdAndStatusOrderByPriorityAscCheckInTimeAsc(
            UUID roomId,
            Queue.QueueStatus status
    );

    /**
     * Đếm số Queue theo room và status
     */
    long countByAssignedRoomAndStatus(ExaminationRoom assignedRoom, Queue.QueueStatus status);

    /**
     * Tìm Queue theo danh sách status, sắp xếp theo priority và checkInTime
     */
    List<Queue> findByStatusInOrderByPriorityAscCheckInTimeAsc(List<Queue.QueueStatus> statuses);
}