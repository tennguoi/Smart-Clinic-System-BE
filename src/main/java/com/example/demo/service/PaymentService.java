package com.example.demo.service;

import com.example.demo.dto.PaymentRequest;
import com.example.demo.dto.PaymentResponse;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PatientRepository patientRepository;
    private final QueueRepository queueRepository;
    private final UserRepository userRepository;
    private final ServiceAssignmentRepository serviceAssignmentRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request, String token) {
        // Get patient
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        // Get queue if provided
        Queue queue = null;
        if (request.queueId() != null) {
            queue = queueRepository.findById(request.queueId())
                    .orElseThrow(() -> new IllegalArgumentException("Queue not found"));
        }

        // Get user processing payment
        String email = jwtUtil.getEmailFromToken(token);
        User processedBy = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Calculate total amount from service assignments if not provided
        BigDecimal totalAmount = request.totalAmount();
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            // Calculate from service assignments
            if (queue != null) {
                // Get medical records for this queue's patient
                // Note: This is simplified - in real scenario, you'd link service assignments to queue
                totalAmount = BigDecimal.ZERO;
            } else {
                totalAmount = request.totalAmount() != null ? request.totalAmount() : BigDecimal.ZERO;
            }
        }

        // Create payment
        Payment payment = Payment.builder()
                .patient(patient)
                .queue(queue)
                .processedBy(processedBy)
                .totalAmount(totalAmount)
                .paidAmount(request.paidAmount() != null ? request.paidAmount() : totalAmount)
                .paymentMethod(request.paymentMethod())
                .status(Payment.Status.Completed)
                .paymentDate(java.time.LocalDateTime.now())
                .notes(request.notes())
                .build();

        payment = paymentRepository.save(payment);
        return toResponse(payment);
    }

    public List<PaymentResponse> getPaymentsByPatient(UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        
        return paymentRepository.findByPatient(patient)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PaymentResponse getPaymentById(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
    }

    @Transactional
    public BigDecimal calculateTotalAmountForQueue(UUID queueId) {
        Queue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new IllegalArgumentException("Queue not found"));

        // Get all service assignments for this patient's medical records
        // This is a simplified version - in real scenario, you'd need to link service assignments to queue
        List<ServiceAssignment> assignments = serviceAssignmentRepository.findByPatient(queue.getPatient());
        
        return assignments.stream()
                .filter(sa -> sa.getStatus() == ServiceAssignment.Status.Completed)
                .map(ServiceAssignment::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getPaymentId(),
                payment.getPatient().getPatientId(),
                payment.getPatient().getFullName(),
                payment.getQueue() != null ? payment.getQueue().getQueueId() : null,
                payment.getQueue() != null ? payment.getQueue().getQueueNumber() : null,
                payment.getProcessedBy().getUserId(),
                payment.getProcessedBy().getFullName(),
                payment.getPaymentCode(),
                payment.getTotalAmount(),
                payment.getPaidAmount(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getPaymentDate(),
                payment.getNotes(),
                payment.getCreatedAt()
        );
    }
}

