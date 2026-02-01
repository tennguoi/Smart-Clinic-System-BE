package com.example.demo.repository;


import com.example.demo.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface AppointmentChatbotRepository extends JpaRepository<Appointment, UUID> {
    // Repository cho chatbot, sử dụng Entity Appointment
}