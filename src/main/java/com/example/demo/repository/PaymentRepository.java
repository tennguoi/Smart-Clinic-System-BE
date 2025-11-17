package com.example.demo.repository;

import com.example.demo.entity.Payment;
import com.example.demo.entity.Patient;
import com.example.demo.entity.Queue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    
    Optional<Payment> findByPaymentCode(String paymentCode);
    
    List<Payment> findByPatient(Patient patient);
    
    List<Payment> findByQueue(Queue queue);
    
    @Query("SELECT p FROM Payment p WHERE p.status = :status ORDER BY p.paymentDate DESC")
    List<Payment> findByStatus(@Param("status") Payment.Status status);
}

