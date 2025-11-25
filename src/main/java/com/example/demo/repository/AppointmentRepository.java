package com.example.demo.repository;

import com.example.demo.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    Optional<Appointment> findByAppointmentCode(String code);
    List<Appointment> findByPhone(String phone);
    List<Appointment> findByPhoneAndStatus(String phone, Appointment.Status status);
    List<Appointment> findByStatus(Appointment.Status status);

    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    long countByStatusAndCreatedAtBetween(Appointment.Status status, java.time.LocalDateTime start, java.time.LocalDateTime end);

    @Query("SELECT new com.example.demo.dto.TrendDataPoint(FUNCTION('DATE_FORMAT', a.createdAt, '%d/%m'), COUNT(a), '') " +
            "FROM Appointment a " +
            "WHERE a.createdAt BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('DATE_FORMAT', a.createdAt, '%d/%m') " +
            "ORDER BY FUNCTION('DATE_FORMAT', a.createdAt, '%d/%m')")
    List<com.example.demo.dto.TrendDataPoint> findTrendBetween(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);

    // Lấy tất cả Appointment trong khoảng thời gian (để xử lý serviceIds)
    @Query("SELECT a FROM Appointment a WHERE a.createdAt BETWEEN :start AND :end")
    List<Appointment> findAllBetween(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);
}
