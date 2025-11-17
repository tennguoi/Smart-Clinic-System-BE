package com.example.demo.repository;

import com.example.demo.entity.Queue;
import com.example.demo.entity.Patient;
import com.example.demo.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QueueRepository extends JpaRepository<Queue, UUID> {
    
    Optional<Queue> findByQueueNumber(String queueNumber);
    
    List<Queue> findByStatusOrderByCheckInTimeAsc(Queue.Status status);
    
    List<Queue> findByStatusAndPriorityOrderByCheckInTimeAsc(Queue.Status status, Queue.Priority priority);
    
    List<Queue> findByPatient(Patient patient);
    
    List<Queue> findByAppointment(Appointment appointment);
    
    @Query("SELECT q FROM Queue q WHERE q.status = :status ORDER BY " +
           "CASE q.priority " +
           "WHEN 'Emergency' THEN 1 " +
           "WHEN 'Urgent' THEN 2 " +
           "WHEN 'Normal' THEN 3 " +
           "END ASC, q.checkInTime ASC")
    List<Queue> findByStatusOrderByPriorityAndCheckInTime(@Param("status") Queue.Status status);
    
    @Query("SELECT q FROM Queue q WHERE q.assignedRoom.roomId = :roomId AND q.status = 'InProgress'")
    Optional<Queue> findCurrentQueueByRoom(@Param("roomId") UUID roomId);
}

