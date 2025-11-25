// src/main/java/com/example/demo/service/BillingService.java
package com.example.demo.service;

import com.example.demo.dto.ExaminationSummaryResponse;
import com.example.demo.dto.billing.*;
import com.example.demo.entity.*;
import com.example.demo.enums.PaymentMethod;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BillingService {

    private final BillingRecordRepository billingRepo;
    private final MedicalRecordRepository medicalRecordRepo;
    private final ServiceRepository serviceRepo;
    private final UserRepository userRepo;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Thêm vào cuối class BillingService.java

    // ==================== TẠO HÓA ĐƠN TỪ EXAMINATION SUMMARY ====================
    @Transactional
    public BillingResponseDTO createInvoiceFromExaminationSummary(
            ExaminationSummaryResponse summary,
            User doctor) {

        // Kiểm tra đã có hóa đơn chưa
        Optional<BillingRecord> existing = billingRepo.findByMedicalRecord_RecordId(summary.recordId());
        if (existing.isPresent()) {
            throw new RuntimeException("Hóa đơn đã được tạo cho buổi khám này");
        }

        // Kiểm tra có dịch vụ không
        if (summary.services() == null || summary.services().isEmpty()) {
            throw new RuntimeException("Chưa có dịch vụ nào để tạo hóa đơn");
        }

        MedicalRecord record = medicalRecordRepo.findById(summary.recordId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ khám"));

        // Tạo hóa đơn mới
        BillingRecord billing = BillingRecord.builder()
                .users(doctor)
                .medicalRecord(record)
                .paymentMethod(PaymentMethod.Cash)  // Mặc định
                .paymentStatus(PaymentStatus.Pending)
                .totalAmount(summary.totalServiceAmount())
                .finalAmount(summary.totalServiceAmount())
                .amountPaid(BigDecimal.ZERO)
                .build();

        // Thêm từng dịch vụ vào hóa đơn
        for (ExaminationSummaryResponse.ExaminationServiceItem item : summary.services()) {
            com.example.demo.entity.Service svc = serviceRepo.findById(item.serviceId())
                    .orElseThrow(() -> new RuntimeException("Dịch vụ không tồn tại: " + item.serviceId()));

            BillingDetail detail = BillingDetail.builder()
                    .billingRecord(billing)
                    .service(svc)
                    .quantity(item.quantity())
                    .unitPrice(item.unitPrice())
                    .subTotal(item.totalPrice())
                    .description(item.note())
                    .build();

            billing.getDetails().add(detail);
        }

        billingRepo.save(billing);
        return toDto(billing);
    }

    private boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role.toUpperCase()));
    }

    // ==================== TẠO HÓA ĐƠN ====================
    public BillingResponseDTO create(BillingCreateDTO dto) {
        // ✅ CHO PHÉP CẢ BÁC SĨ VÀ LỄ TÂN
        if (!hasRole("TIEP_TAN") && !hasRole("BAC_SI"))
            throw new AccessDeniedException("Chỉ bác sĩ và lễ tân được tạo hóa đơn");

        User currentUser = getCurrentUser();
        MedicalRecord record = medicalRecordRepo.findById(dto.recordId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ khám"));

        BillingRecord billing = BillingRecord.builder()
                .users(currentUser)
                .medicalRecord(record)
                .paymentMethod(dto.paymentMethod())
                .paymentStatus(PaymentStatus.Pending)
                .totalAmount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.ZERO)
                .amountPaid(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (BillingItemDTO item : dto.items()) {
            com.example.demo.entity.Service svc = serviceRepo.findById(item.serviceId())
                    .orElseThrow(() -> new RuntimeException("Dịch vụ không tồn tại: " + item.serviceId()));

            BigDecimal subTotal = svc.getPrice().multiply(BigDecimal.valueOf(item.quantity()));

            BillingDetail detail = BillingDetail.builder()
                    .billingRecord(billing)
                    .service(svc)
                    .description(item.description())
                    .quantity(item.quantity())
                    .unitPrice(svc.getPrice())
                    .subTotal(subTotal)
                    .build();

            billing.getDetails().add(detail);
            total = total.add(subTotal);
        }

        billing.setTotalAmount(total);
        billing.setFinalAmount(total);
        billingRepo.save(billing);

        return toDto(billing);
    }

    // ==================== THANH TOÁN ====================
    public BillingResponseDTO pay(UUID billId, BigDecimal paidAmount, PaymentMethod method) {
        if (!hasRole("TIEP_TAN"))
            throw new AccessDeniedException("Chỉ lễ tân được thu tiền");

        BillingRecord billing = billingRepo.findById(billId)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        if (billing.getPaymentStatus() == PaymentStatus.Paid)
            throw new RuntimeException("Hóa đơn đã thanh toán");

        BigDecimal newPaid = billing.getAmountPaid().add(paidAmount);
        if (newPaid.compareTo(billing.getFinalAmount()) > 0)
            throw new RuntimeException("Số tiền thanh toán vượt quá công nợ");

        billing.setAmountPaid(newPaid);
        billing.setPaymentMethod(method);

        if (newPaid.compareTo(billing.getFinalAmount()) >= 0) {
            billing.setAmountPaid(billing.getFinalAmount());
            billing.setPaymentStatus(PaymentStatus.Paid);
            billing.setPaidAt(LocalDateTime.now());
        }

        billingRepo.save(billing);
        return toDto(billing);
    }

    // ==================== LẤY HÓA ĐƠN CHỜ THANH TOÁN ====================
    public Page<BillingResponseDTO> getPendingBills(Pageable pageable, String search) {
        if (!hasRole("BAC_SI") && !hasRole("TIEP_TAN"))
            throw new AccessDeniedException("Không có quyền");

        Page<BillingRecord> page;

        if (search == null || search.isBlank()) {
            page = billingRepo.findByPaymentStatus(PaymentStatus.Pending, pageable);
        } else {
            page = billingRepo.searchPending(search, pageable);
        }

        return page.map(this::toDto);
    }

    // ==================== LẤY TẤT CẢ HÓA ĐƠN ====================
    public Page<BillingResponseDTO> getAll(Pageable pageable, String search) {
        if (!hasRole("BAC_SI") && !hasRole("TIEP_TAN"))
            throw new AccessDeniedException("Không có quyền");

        Page<BillingRecord> page = (search == null || search.isBlank())
                ? billingRepo.findAll(pageable)
                : billingRepo.search(search, pageable);

        return page.map(this::toDto);
    }

    // ==================== LẤY CHI TIẾT HÓA ĐƠN ====================
    public BillingResponseDTO getById(UUID id) {
        if (!hasRole("BAC_SI") && !hasRole("TIEP_TAN"))
            throw new AccessDeniedException("Không có quyền");

        return toDto(billingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại")));
    }

    // ==================== HELPER: CONVERT TO DTO ====================
    private BillingResponseDTO toDto(BillingRecord b) {
        var items = b.getDetails().stream()
                .map(d -> new BillingItemResponseDTO(
                        d.getService().getName(),
                        d.getQuantity(),
                        d.getUnitPrice(),
                        d.getSubTotal(),
                        d.getDescription()))
                .toList();

        BigDecimal remaining = b.getFinalAmount().subtract(b.getAmountPaid()).max(BigDecimal.ZERO);

        return new BillingResponseDTO(
                b.getBillId(),
                b.getMedicalRecord().getPatient().getFullName(),
                b.getMedicalRecord().getPatient().getPhone(),
                b.getCreatedAt(),
                b.getUsers().getFullName(),
                b.getTotalAmount(),
                b.getFinalAmount(),
                b.getAmountPaid(),
                remaining,
                b.getPaymentMethod(),
                b.getPaymentStatus(),
                b.getPaidAt(),
                items
        );
    }
}