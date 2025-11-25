package com.example.demo.repository;

import com.example.demo.entity.ExaminationRoom;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExaminationRoomRepository extends JpaRepository<ExaminationRoom, UUID> {
    Optional<ExaminationRoom> findByDoctorAndStatusAndIsActiveTrue(
            User doctor,
            ExaminationRoom.RoomStatus status
    );
    
    // Tìm phòng active của bác sĩ (dùng userId để so sánh chính xác, eager fetch doctor)
    @Query("SELECT r FROM ExaminationRoom r LEFT JOIN FETCH r.doctor WHERE r.doctor.userId = :doctorId AND r.isActive = true")
    Optional<ExaminationRoom> findByDoctorIdAndIsActiveTrue(UUID doctorId);
    
    Optional<ExaminationRoom> findByDoctorAndIsActiveTrue(User doctor);
    List<ExaminationRoom> findByIsActiveTrue();

    List<ExaminationRoom> findByStatus(ExaminationRoom.RoomStatus status);

    List<ExaminationRoom> findByDoctorUserId(UUID doctorId);



    // === METHOD MỚI MÀ SERVICE ĐANG GỌI ===
    @Query("SELECT r FROM ExaminationRoom r WHERE r.isActive = true AND r.doctor IS NOT NULL ORDER BY r.roomName ASC")
    List<ExaminationRoom> findByIsActiveTrueAndDoctorNotNullOrderByRoomNameAsc();

    // Các method khác nếu có...

    /**
     * Tìm phòng active của một bác sĩ (để đảm bảo 1 bác sĩ chỉ ở 1 phòng)
     */
    @Query("SELECT r FROM ExaminationRoom r WHERE r.doctor.userId = :doctorId AND r.isActive = true")
    List<ExaminationRoom> findActiveRoomsByDoctorId(UUID doctorId);

    /**
     * Tìm phòng active của một bác sĩ, loại trừ một phòng cụ thể (dùng khi cập nhật)
     */
    @Query("SELECT r FROM ExaminationRoom r WHERE r.doctor.userId = :doctorId AND r.isActive = true AND r.roomId != :excludeRoomId")
    List<ExaminationRoom> findActiveRoomsByDoctorIdExcludingRoom(UUID doctorId, UUID excludeRoomId);

    @Query("SELECT r FROM ExaminationRoom r WHERE r.isActive = true AND " +
           "(LOWER(r.roomName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<ExaminationRoom> searchActiveRooms(String keyword);

    Optional<ExaminationRoom> findByRoomName(String roomName);
    List<ExaminationRoom> findByIsActiveTrueOrderByRoomNameAsc();
}

