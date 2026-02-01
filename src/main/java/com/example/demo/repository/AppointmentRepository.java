// src/main/java/com/example/demo/repository/AppointmentRepository.java
package com.example.demo.repository;

import com.example.demo.dto.TrendDataPoint;
import com.example.demo.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    Optional<Appointment> findByAppointmentCode(String code);

    List<Appointment> findByPhone(String phone);

    List<Appointment> findByPhoneAndStatus(String phone, Appointment.Status status);

    List<Appointment> findByStatus(Appointment.Status status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByStatusAndCreatedAtBetween(Appointment.Status status, LocalDateTime start, LocalDateTime end);

    // ✨ PHƯƠNG THỨC MỚI: Tìm các appointment đã xác nhận trong khoảng thời gian
    /**
     * Tìm các appointment theo status và appointmentTime trong khoảng thời gian
     * Dùng cho check-in lễ tân
     */
    List<Appointment> findByStatusAndAppointmentTimeBetween(
            Appointment.Status status,
            LocalDateTime start,
            LocalDateTime end
    );

    /**
     * Dùng cho dashboard trend
     */
    @Query(
            "SELECT new com.example.demo.dto.TrendDataPoint(" +
                    "  FUNCTION('DATE_FORMAT', a.createdAt, '%d/%m'), COUNT(a), ''" +
                    ") " +
                    "FROM Appointment a " +
                    "WHERE a.createdAt BETWEEN :start AND :end " +
                    "GROUP BY FUNCTION('DATE_FORMAT', a.createdAt, '%d/%m') " +
                    "ORDER BY FUNCTION('DATE_FORMAT', a.createdAt, '%d/%m')"
    )
    List<TrendDataPoint> findTrendBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Lấy tất cả Appointment trong khoảng thời gian (để xử lý serviceIds)
    @Query("SELECT a FROM Appointment a WHERE a.createdAt BETWEEN :start AND :end")
    List<Appointment> findAllBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Tìm kiếm + phân trang cho lễ tân
     * - Nếu keyword null hoặc rỗng => bỏ điều kiện tìm theo tên/phone
     */
    @Query(
            "SELECT a FROM Appointment a " +
                    "WHERE a.status = :status " +
                    "  AND ( " +
                    "       :keyword IS NULL " +
                    "       OR :keyword = '' " +
                    "       OR LOWER(a.patientName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "       OR a.phone LIKE CONCAT('%', :keyword, '%') " +
                    "      ) " +
                    "ORDER BY a.createdAt DESC"
    )
    Page<Appointment> search(
            @Param("status") Appointment.Status status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT a FROM Appointment a WHERE a.status = :status AND DATE(a.appointmentTime) = :date")
    List<Appointment> findByStatusAndAppointmentTimeDate(Appointment.Status status, LocalDate date);

    // ✨ PHƯƠNG THỨC THÊM: Lấy appointment trong ngày hôm nay
    /**
     * Tìm các appointment theo status trong ngày cụ thể
     * Dùng cho check-in nhanh
     */
    @Query("SELECT a FROM Appointment a " +
            "WHERE a.status = :status " +
            "AND DATE(a.appointmentTime) = :date " +
            "ORDER BY a.appointmentTime ASC")
    List<Appointment> findByStatusAndAppointmentDate(
            @Param("status") Appointment.Status status,
            @Param("date") LocalDate date
    );
}