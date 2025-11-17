package com.example.demo.repository;

import com.example.demo.entity.ExaminationRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExaminationRoomRepository extends JpaRepository<ExaminationRoom, UUID> {

    List<ExaminationRoom> findByIsActiveTrue();

    List<ExaminationRoom> findByStatus(ExaminationRoom.RoomStatus status);

    List<ExaminationRoom> findByDoctorUserId(UUID doctorId);

    @Query("SELECT r FROM ExaminationRoom r WHERE r.isActive = true AND " +
           "(LOWER(r.roomName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<ExaminationRoom> searchActiveRooms(String keyword);

    Optional<ExaminationRoom> findByRoomName(String roomName);
    List<ExaminationRoom> findByIsActiveTrueOrderByRoomNameAsc();
}

