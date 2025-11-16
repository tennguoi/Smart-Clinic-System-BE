package com.example.demo.repository;

import com.example.demo.entity.Queue;
import com.example.demo.entity.Queue.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QueueRepository extends JpaRepository<Queue, UUID> {

    List<Queue> findByStatusOrderByCheckInTimeAsc(QueueStatus status);
    Queue findTopByOrderByCreatedAtDesc();
}
