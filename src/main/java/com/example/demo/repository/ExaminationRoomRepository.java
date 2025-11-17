package com.example.demo.repository;

import com.example.demo.entity.ExaminationRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExaminationRoomRepository extends JpaRepository<ExaminationRoom, UUID> {
    
    Optional<ExaminationRoom> findByRoomNumber(String roomNumber);
    
    List<ExaminationRoom> findByStatus(ExaminationRoom.Status status);
    
    @Query("SELECT r FROM ExaminationRoom r WHERE r.status = 'Available' ORDER BY r.roomNumber ASC")
    List<ExaminationRoom> findAvailableRooms();
    
    @Query("SELECT r FROM ExaminationRoom r WHERE r.assignedDoctor.userId = :doctorId")
    List<ExaminationRoom> findByAssignedDoctor(@Param("doctorId") UUID doctorId);
}

