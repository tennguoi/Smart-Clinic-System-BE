package com.example.demo.repository;

import com.example.demo.dto.TopServiceDTO;
import com.example.demo.entity.InvoiceDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, Long> {

    // Đây là câu lệnh quan trọng nhất: Tính tổng số lượng bán ra của từng dịch vụ
    @Query("SELECT new com.example.demo.dto.TopServiceDTO(d.service, SUM(d.quantity)) " +
            "FROM InvoiceDetail d " +
            "GROUP BY d.service " +
            "ORDER BY SUM(d.quantity) DESC")
    List<TopServiceDTO> findTopServices(Pageable pageable);

    // Tính tổng số lượng bán ra của từng dịch vụ trong khoảng thời gian
    @Query("SELECT new com.example.demo.dto.TopServiceDTO(d.service, SUM(d.quantity)) " +
            "FROM InvoiceDetail d " +
            "WHERE d.invoice.createdAt BETWEEN :start AND :end " +
            "GROUP BY d.service " +
            "ORDER BY SUM(d.quantity) DESC")
    List<TopServiceDTO> findTopServicesBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    // Top dịch vụ theo doanh thu (tổng amount)
    @Query("SELECT new com.example.demo.dto.TopServiceDTO(d.service, SUM(d.amount)) " +
            "FROM InvoiceDetail d " +
            "WHERE d.invoice.createdAt BETWEEN :start AND :end " +
            "GROUP BY d.service " +
            "ORDER BY SUM(d.amount) DESC")
    List<TopServiceDTO> findTopServicesByRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);
}