// src/main/java/com/example/demo/service/InvoiceService.java
package com.example.demo.service;

import com.example.demo.dto.invoice.*;
import com.example.demo.entity.*;
import com.example.demo.enums.InvoiceStatus;
import com.example.demo.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PatientRepository patientRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    private Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean hasRole(String roleName) {
        return getCurrentAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + roleName));
    }

    private String getCurrentUserEmail() {
        return getCurrentAuthentication().getName();
    }

    // TẠO HÓA ĐƠN – TÍNH TIỀN CHÍNH XÁC 100%
    public InvoiceResponseDTO createInvoice(InvoiceCreateDTO dto) {
        if (!hasRole("TIEP_TAN")) {
            throw new AccessDeniedException("Chỉ lễ tân được phép tạo hóa đơn");
        }

        User currentUser = userRepository.findByEmail(getCurrentUserEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Patient patient = patientRepository.findById(dto.patientId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân"));

        Invoice invoice = Invoice.builder()
                .invoiceCode(generateInvoiceCode())
                .patient(patient)
                .createdBy(currentUser)
                .status(InvoiceStatus.ISSUED)
                .paidAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (InvoiceItemDTO item : dto.items()) {
            com.example.demo.entity.Service service = serviceRepository.findById(item.serviceId())
                    .orElseThrow(() -> new RuntimeException("Dịch vụ không tồn tại: " + item.serviceId()));

            BigDecimal unitPrice = service.getPrice();
            int quantity = item.quantity();
            BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(quantity));

            InvoiceDetail detail = InvoiceDetail.builder()
                    .invoice(invoice)
                    .service(service)
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .amount(amount)  // TÍNH RÕ RÀNG TẠI ĐÂY – KHÔNG TIN VÀO @PrePersist
                    .build();

            invoice.getDetails().add(detail);
            totalAmount = totalAmount.add(amount);
        }

        invoice.setTotalAmount(totalAmount);
        invoice.setRemainingAmount(totalAmount);

        invoiceRepository.save(invoice);

        return toResponseDTO(invoice);
    }

    // THANH TOÁN HÓA ĐƠN
    public InvoiceResponseDTO payInvoice(UUID invoiceId, BigDecimal paidAmount) {
        if (!hasRole("TIEP_TAN")) {
            throw new AccessDeniedException("Chỉ lễ tân được phép thu tiền");
        }

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        BigDecimal newPaid = invoice.getPaidAmount().add(paidAmount);
        BigDecimal newRemaining = invoice.getRemainingAmount().subtract(paidAmount);

        invoice.setPaidAmount(newPaid);
        invoice.setRemainingAmount(newRemaining.max(BigDecimal.ZERO));

        if (newRemaining.compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidAmount(invoice.getTotalAmount());
            invoice.setRemainingAmount(BigDecimal.ZERO);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        invoiceRepository.save(invoice);
        return toResponseDTO(invoice);
    }

    // DANH SÁCH HÓA ĐƠN
    public Page<InvoiceResponseDTO> getAll(Pageable pageable, String search) {
        if (!hasRole("BAC_SI") && !hasRole("TIEP_TAN")) {
            throw new AccessDeniedException("Không có quyền xem hóa đơn");
        }

        Page<Invoice> page = search == null || search.isBlank()
                ? invoiceRepository.findAll(pageable)
                : invoiceRepository.findAllByPatientFullNameContainingIgnoreCase(search, pageable);

        return page.map(this::toResponseDTO);
    }

    // CHI TIẾT HÓA ĐƠN
    public InvoiceResponseDTO getById(UUID id) {
        if (!hasRole("BAC_SI") && !hasRole("TIEP_TAN")) {
            throw new AccessDeniedException("Không có quyền xem hóa đơn");
        }
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));
        return toResponseDTO(invoice);
    }

    // TẠO MÃ HÓA ĐƠN TỰ ĐỘNG
    private String generateInvoiceCode() {
        String prefix = "HD" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        long count = invoiceRepository.countByCreatedAtBetween(start, end);
        return prefix + String.format("%04d", count + 1);
    }

    // CHUYỂN ENTITY → DTO
    private InvoiceResponseDTO toResponseDTO(Invoice i) {
        var items = i.getDetails().stream()
                .map(d -> new InvoiceItemResponseDTO(
                        d.getService().getName(),
                        d.getQuantity(),
                        d.getUnitPrice(),
                        d.getAmount()
                ))
                .toList();

        return new InvoiceResponseDTO(
                i.getInvoiceId(),
                i.getInvoiceCode(),
                i.getPatient().getFullName(),
                i.getPatient().getPhone(),
                i.getCreatedAt(),
                i.getCreatedBy().getFullName(),
                i.getTotalAmount(),
                i.getPaidAmount(),
                i.getRemainingAmount(),
                i.getStatus().name(),
                items
        );
    }
}