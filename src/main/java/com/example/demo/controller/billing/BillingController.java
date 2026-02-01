package com.example.demo.controller.billing;

import com.example.demo.dto.billing.*;
import com.example.demo.entity.BillingRecord;
import com.example.demo.enums.PaymentMethod;
import com.example.demo.repository.BillingRecordRepository;
import com.example.demo.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
@Slf4j
public class BillingController {

    private final BillingService billingService;
    private final BillingRecordRepository billingRecordRepository;

    // ✅ TẠO HÓA ĐƠN THỦ CÔNG
    @PostMapping
    @PreAuthorize("hasAnyRole('TIEP_TAN', 'BAC_SI')")
    public ResponseEntity<BillingResponseDTO> create(@Valid @RequestBody BillingCreateDTO dto) {
        return ResponseEntity.ok(billingService.create(dto));
    }

    // ✅ LẤY HÓA ĐƠN CHỜ THANH TOÁN
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('BAC_SI', 'TIEP_TAN', 'ADMIN')")
    public ResponseEntity<Page<BillingResponseDTO>> getPendingBills(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(billingService.getPendingBills(pageable, search));
    }

    // ✅ THANH TOÁN
    @PutMapping("/{id}/pay")
    @PreAuthorize("hasRole('TIEP_TAN')")
    public ResponseEntity<BillingResponseDTO> pay(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {

        BigDecimal paidAmount = new BigDecimal(body.get("paidAmount").toString());
        PaymentMethod method = PaymentMethod.valueOf(body.get("paymentMethod").toString());

        return ResponseEntity.ok(billingService.pay(id, paidAmount, method));
    }

    // ✅ LẤY TẤT CẢ HÓA ĐƠN
    @GetMapping
    @PreAuthorize("hasAnyRole('TIEP_TAN', 'ADMIN')")
    public ResponseEntity<Page<BillingResponseDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(billingService.getAll(pageable, search));
    }

    // ✅ CHI TIẾT HÓA ĐƠN
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BAC_SI', 'TIEP_TAN', 'ADMIN')")
    public ResponseEntity<BillingResponseDTO> getDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(billingService.getById(id));
    }

    // ✅ CẬP NHẬT HÓA ĐƠN (CHỈ KHI CHƯA THANH TOÁN)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TIEP_TAN')")
    public ResponseEntity<BillingResponseDTO> updateInvoice(
            @PathVariable UUID id,
            @Valid @RequestBody BillingUpdateDTO dto) {
        return ResponseEntity.ok(billingService.updateInvoice(id, dto));
    }

    // ✅ KIỂM TRA TRẠNG THÁI THANH TOÁN - CHO POLLING FRONTEND
    /**
     * Endpoint này được frontend polling mỗi 10 giây khi hiển thị QR code
     * Để tự động phát hiện khi có thanh toán từ Casso hoặc manual update
     */
    @GetMapping("/{billId}/payment-status")
    @PreAuthorize("hasAnyRole('BAC_SI', 'TIEP_TAN')")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable UUID billId) {
        try {
            BillingRecord billing = billingRecordRepository.findById(billId)
                    .orElseThrow(() -> new RuntimeException("Bill not found with ID: " + billId));

            // Tính toán số tiền còn lại
            BigDecimal remainingAmount = billing.getFinalAmount().subtract(billing.getAmountPaid());

            // Kiểm tra đã thanh toán đủ chưa
            boolean isPaid = "Paid".equals(billing.getPaymentStatus().toString()) ||
                    billing.getAmountPaid().compareTo(billing.getFinalAmount()) >= 0;

            Map<String, Object> response = new HashMap<>();
            response.put("billId", billing.getBillId().toString());
            response.put("paymentStatus", billing.getPaymentStatus().toString());
            response.put("amountPaid", billing.getAmountPaid());
            response.put("finalAmount", billing.getFinalAmount());
            response.put("remainingAmount", remainingAmount);
            response.put("isPaid", isPaid);
            response.put("paymentMethod", billing.getPaymentMethod().toString());
            response.put("lastUpdated", billing.getPaidAt());

            log.debug("Payment status check for bill {}: isPaid={}, remaining={}",
                    billId, isPaid, remainingAmount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting payment status for bill {}: {}", billId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", e.getMessage(),
                            "billId", billId.toString()
                    ));
        }
    }
    @GetMapping("/my-bills")
    @PreAuthorize("hasAnyRole('BAC_SI', 'TIEP_TAN', 'ADMIN')")
    public ResponseEntity<Page<BillingResponseDTO>> getMyBills(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(billingService.getMyBills(pageable, search));
    }
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('BAC_SI', 'TIEP_TAN', 'ADMIN')")
    public ResponseEntity<byte[]> getInvoicePdf(@PathVariable UUID id) {
        byte[] pdfBytes = billingService.getInvoicePdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}