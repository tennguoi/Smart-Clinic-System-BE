// src/main/java/com/example/demo/service/BillingService.java
package com.example.demo.service;

import com.example.demo.dto.billing.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BillingService {

    private final BillingRecordRepository billingRepo;
    private final MedicalRecordRepository medicalRecordRepo;
    private final ServiceRepository serviceRepo;        // giữ nguyên tên repo cũ
    private final UserRepository userRepo;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // FIX: đúng cách kiểm tra role
    private boolean hasRole(String role) {
        String roleWithPrefix = "ROLE_" + role.toUpperCase();
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(roleWithPrefix));
    }

    // TẠO HÓA ĐƠN
    public BillingResponseDTO create(BillingCreateDTO dto) {
        if (!hasRole("TIEP_TAN")) throw new AccessDeniedException("Chỉ lễ tân được tạo hóa đơn");

        User receptionist = getCurrentUser();
        MedicalRecord record = medicalRecordRepo.findById(dto.recordId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ khám"));

        BillingRecord billing = BillingRecord.builder()
                .users(receptionist)
                .medicalRecord(record)
                .paymentMethod(dto.paymentMethod())
                .paymentStatus(PaymentStatus.Pending)
                .totalAmount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.ZERO)
                .amountPaid(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (BillingItemDTO item : dto.items()) {
            // FIX: dùng đúng entity Service của bạn (String id)
            com.example.demo.entity.Service svc = serviceRepo.findById(item.serviceId())
                    .orElseThrow(() -> new RuntimeException("Dịch vụ không tồn tại: " + item.serviceId()));

            BigDecimal subTotal = svc.getPrice().multiply(BigDecimal.valueOf(item.quantity()));

            BillingDetail detail = BillingDetail.builder()
                    .billingRecord(billing)
                    .service(svc)                               // tự động hiểu đúng nhờ import
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

    // THANH TOÁN
    public BillingResponseDTO pay(UUID billId, BigDecimal paidAmount, PaymentMethod method) {
        if (!hasRole("TIEP_TAN")) throw new AccessDeniedException("Chỉ lễ tân được thu tiền");

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

    public Page<BillingResponseDTO> getAll(Pageable pageable, String search) {
        if (!hasRole("BAC_SI") && !hasRole("TIEP_TAN"))
            throw new AccessDeniedException("Không có quyền");

        Page<BillingRecord> page = (search == null || search.isBlank())
                ? billingRepo.findAll(pageable)
                : billingRepo.search(search, pageable);

        return page.map(this::toDto);
    }

    public BillingResponseDTO getById(UUID id) {
        if (!hasRole("BAC_SI") && !hasRole("TIEP_TAN"))
            throw new AccessDeniedException("Không có quyền");

        return toDto(billingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại")));
    }

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