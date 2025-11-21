package com.example.demo.repository;

import com.example.demo.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    Optional<Appointment> findByAppointmentCode(String code);
    List<Appointment> findByPhone(String phone);
    List<Appointment> findByPhoneAndStatus(String phone, Appointment.Status status);
    List<Appointment> findByStatus(Appointment.Status status);
    List<Appointment> findByAppointmentTimeBetweenAndReminderSentFalse(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
