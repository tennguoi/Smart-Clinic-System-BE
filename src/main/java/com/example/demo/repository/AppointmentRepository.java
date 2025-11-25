// src/main/java/com/example/demo/repository/AppointmentRepository.java
package com.example.demo.repository;

import com.example.demo.dto.TrendDataPoint;
import com.example.demo.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    List<Appointment> findAllBetween(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);
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
}
