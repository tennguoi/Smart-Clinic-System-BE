// src/main/java/com/example/demo/service/AppointmentService.java
package com.example.demo.service;

import com.example.demo.dto.appointment.AppointmentRequest;
import com.example.demo.dto.appointment.AppointmentResponse;
import com.example.demo.dto.ServiceResponse;
import com.example.demo.entity.Appointment;
import com.example.demo.entity.Invoice;
import com.example.demo.entity.InvoiceDetail;
import com.example.demo.entity.Patient;
import com.example.demo.entity.User;
import com.example.demo.enums.InvoiceStatus;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.InvoiceDetailRepository;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.PatientRepository;
import com.example.demo.repository.ServiceRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.CodeGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {

    private final AppointmentRepository repo;
    private final UserRepository userRepo;
    private final ServiceRepository serviceRepo;
    private final EmailService emailService;
    private final PatientRepository patientRepo;
    private final InvoiceRepository invoiceRepo;
    private final InvoiceDetailRepository invoiceDetailRepo;

    public AppointmentService(AppointmentRepository repo,
                              UserRepository userRepo,
                              ServiceRepository serviceRepo,
                              EmailService emailService,
                              PatientRepository patientRepo,
                              InvoiceRepository invoiceRepo,
                              InvoiceDetailRepository invoiceDetailRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.serviceRepo = serviceRepo;
        this.emailService = emailService;
        this.patientRepo = patientRepo;
        this.invoiceRepo = invoiceRepo;
        this.invoiceDetailRepo = invoiceDetailRepo;
    }

    // =========================================================
    // 1. KHÁCH TỰ ĐẶT LỊCH (QUICK BOOK)
    // =========================================================

    @Transactional
    public AppointmentResponse quickBook(AppointmentRequest req) {
        if (req.appointmentTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian hẹn phải từ thời điểm hiện tại trở về sau.");
        }

        Appointment entity = Appointment.builder()
                .patientName(req.patientName())
                .phone(req.phone())
                .email(req.email())
                .appointmentTime(req.appointmentTime())
                .notes(req.notes())
                .quickBook(true)
                .serviceIds(joinServiceIds(req.serviceIds()))
                .appointmentCode(generateUniqueCode())
                .build();

        entity = repo.save(entity);

        // Gửi email pending nếu có email
        if (entity.getEmail() != null && !entity.getEmail().isBlank()) {
            try {
                String serviceNames = "";
                if (req.serviceIds() != null && !req.serviceIds().isEmpty()) {
                    List<com.example.demo.entity.Service> services = serviceRepo.findAllById(req.serviceIds());
                    serviceNames = services.stream()
                            .map(com.example.demo.entity.Service::getName)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("");
                }

                emailService.sendAppointmentPendingNotification(
                        entity.getEmail(),
                        entity.getPatientName(),
                        entity.getAppointmentCode(),
                        entity.getAppointmentTime(),
                        entity.getPhone(),
                        entity.getNotes(),
                        serviceNames
                );
            } catch (Exception e) {
                System.err.println("Failed to send pending appointment email: " + e.getMessage());
            }
        }

        // Tạo invoice nháp nếu có chọn dịch vụ
        if (req.serviceIds() != null && !req.serviceIds().isEmpty()) {
            try {
                Patient patient = findOrCreatePatient(req);
                createDraftInvoice(patient, req.serviceIds());
            } catch (Exception e) {
                System.err.println("Failed to create draft invoice: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return toResponse(entity);
    }

    // =========================================================
    // 2. LỄ TÂN TẠO LỊCH
    // =========================================================

    @Transactional
    public AppointmentResponse create(AppointmentRequest req) {
        if (req.appointmentTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian hẹn phải từ thời điểm hiện tại trở về sau.");
        }

        Appointment entity = Appointment.builder()
                .patientName(req.patientName())
                .phone(req.phone())
                .email(req.email())
                .appointmentTime(req.appointmentTime())
                .notes(req.notes())
                .quickBook(false)
                .serviceIds(joinServiceIds(req.serviceIds()))
                .appointmentCode(generateUniqueCode())
                .build();

        entity = repo.save(entity);

        if (req.serviceIds() != null && !req.serviceIds().isEmpty()) {
            try {
                Patient patient = findOrCreatePatient(req);
                createDraftInvoice(patient, req.serviceIds());
            } catch (Exception e) {
                System.err.println("Failed to create draft invoice: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return toResponse(entity);
    }

    // =========================================================
    // 3. LIST CŨ (NẾU CÒN DÙNG)
    // =========================================================

    public List<AppointmentResponse> listByStatus(Appointment.Status status) {
        return repo.findByStatus(status).stream()
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .map(this::toResponse)
                .toList();
    }

    public List<AppointmentResponse> listBy(Appointment.Status status, String phone, String patientName) {
        var results = repo.findByStatus(status);

        if (phone != null && !phone.isBlank()) {
            String searchPhone = phone.trim();
            results = results.stream()
                    .filter(a -> a.getPhone().contains(searchPhone))
                    .toList();
        }
        if (patientName != null && !patientName.isBlank()) {
            String searchName = patientName.trim().toLowerCase();
            results = results.stream()
                    .filter(a -> a.getPatientName().toLowerCase().contains(searchName))
                    .toList();
        }

        return results.stream()
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // 4. TÌM KIẾM + PHÂN TRANG CHO LỄ TÂN
    // =========================================================

    public Page<AppointmentResponse> searchAppointments(String status, String keyword, int page, int size) {
        Appointment.Status st;
        try {
            st = Appointment.Status.valueOf(status.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + status);
        }

        // keyword rỗng => truyền null để repo bỏ filter LIKE
        String kw = (keyword == null || keyword.trim().isEmpty())
                ? null
                : keyword.trim();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<Appointment> data = repo.search(st, kw, pageable);
        return data.map(this::toResponse);
    }

    // =========================================================
    // 5. CẬP NHẬT TRẠNG THÁI
    // =========================================================

    @Transactional
    public AppointmentResponse updateStatus(UUID id, Appointment.Status status) {
        Appointment appt = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        appt.setStatus(status);

        if (status == Appointment.Status.Confirmed && appt.getConfirmedBy() == null) {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getName() == null) {
                throw new IllegalStateException("Authentication required to confirm appointment");
            }
            String username = authentication.getName();
            User confirmedBy = userRepo.findByEmail(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            appt.setConfirmedBy(confirmedBy);
            appt.setConfirmedAt(LocalDateTime.now());

            if (appt.getEmail() != null && !appt.getEmail().isEmpty()) {
                try {
                    String formattedTime = appt.getAppointmentTime()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    emailService.sendAppointmentConfirmation(
                            appt.getEmail(),
                            appt.getPatientName(),
                            appt.getAppointmentCode(),
                            formattedTime,
                            confirmedBy.getFullName()
                    );
                } catch (Exception e) {
                    System.err.println("Failed to send confirmation email: " + e.getMessage());
                }
            }
        }
        appt = repo.save(appt);
        return toResponse(appt);
    }

    // =========================================================
    // 6. CẬP NHẬT THÔNG TIN LỊCH HẸN
    // =========================================================

    @Transactional
    public AppointmentResponse updateInfo(UUID id, AppointmentRequest req) {
        if (req.appointmentTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian hẹn mới không được ở quá khứ.");
        }

        Appointment appt = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with id: " + id));

        appt.setPatientName(req.patientName());
        appt.setPhone(req.phone());
        appt.setEmail(req.email());
        appt.setAppointmentTime(req.appointmentTime());
        appt.setNotes(req.notes());
        appt.setServiceIds(joinServiceIds(req.serviceIds()));

        appt = repo.save(appt);
        return toResponse(appt);
    }

    // =========================================================
    // 7. HELPERS
    // =========================================================

    private String joinServiceIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) return null;
        return String.join(",", ids);
    }

    private AppointmentResponse toResponse(Appointment a) {
        String confirmedByName = a.getConfirmedBy() != null ? a.getConfirmedBy().getFullName() : null;

        List<String> serviceIdsList = null;
        List<ServiceResponse> serviceResponses = new ArrayList<>();

        if (a.getServiceIds() != null && !a.getServiceIds().isBlank()) {
            serviceIdsList = List.of(a.getServiceIds().split(","))
                    .stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            if (!serviceIdsList.isEmpty()) {
                List<com.example.demo.entity.Service> services = serviceRepo.findAllById(serviceIdsList);
                serviceResponses = services.stream()
                        .map(s -> new ServiceResponse(
                                s.getServiceId(),
                                s.getName(),
                                s.getDescription(),
                                s.getCategory().getDisplayName(),
                                s.getPrice(),
                                s.getIsActive(),
                                s.getPhotoUrl()
                        ))
                        .toList();
            }
        }

        return new AppointmentResponse(
                a.getAppointmentId(),
                a.getPatientName(),
                a.getPhone(),
                a.getEmail(),
                a.getAppointmentTime(),
                a.getStatus(),
                a.getAppointmentCode(),
                a.getNotes(),
                a.getQuickBook(),
                serviceIdsList,
                serviceResponses,
                a.getCreatedAt(),
                a.getConfirmedAt(),
                confirmedByName
        );
    }

    private String generateUniqueCode() {
        String code;
        int attempts = 0;
        do {
            code = CodeGenerator.generate(8);
            attempts++;
            if (attempts > 100) throw new IllegalStateException("Cannot generate unique code");
        } while (repo.findByAppointmentCode(code).isPresent());
        return code;
    }

    private Patient findOrCreatePatient(AppointmentRequest req) {
        return patientRepo.findByPhone(req.phone())
                .orElseGet(() -> {
                    Patient newPatient = Patient.builder()
                            .fullName(req.patientName())
                            .phone(req.phone())
                            .email(req.email())
                            .dob(LocalDate.of(2000, 1, 1))
                            .gender(Patient.Gender.other)
                            .address("Unknown")
                            .build();
                    return patientRepo.save(newPatient);
                });
    }

    private void createDraftInvoice(Patient patient, List<String> serviceIds) {
        if (serviceIds == null || serviceIds.isEmpty()) return;

        List<com.example.demo.entity.Service> services = serviceRepo.findAllById(serviceIds);
        if (services.isEmpty()) return;

        User systemUser = userRepo.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No users found"));

        Invoice invoice = new Invoice();
        invoice.setInvoiceCode(generateUniqueInvoiceCode());
        invoice.setPatient(patient);
        invoice.setCreatedBy(systemUser);
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setTotalAmount(BigDecimal.ZERO);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setRemainingAmount(BigDecimal.ZERO);

        invoice = invoiceRepo.save(invoice);

        BigDecimal total = BigDecimal.ZERO;
        List<InvoiceDetail> details = new ArrayList<>();

        for (com.example.demo.entity.Service s : services) {
            InvoiceDetail detail = new InvoiceDetail();
            detail.setInvoice(invoice);
            detail.setService(s);
            detail.setQuantity(1);
            detail.setUnitPrice(s.getPrice());
            detail.setAmount(s.getPrice());
            details.add(detail);

            total = total.add(s.getPrice());
        }

        invoice.setTotalAmount(total);
        invoice.setRemainingAmount(total);

        invoiceDetailRepo.saveAll(details);
        invoiceRepo.save(invoice);
    }

    private String generateUniqueInvoiceCode() {
        String code;
        int attempts = 0;
        do {
            code = "INV-" + CodeGenerator.generate(6);
            attempts++;
            if (attempts > 100) throw new IllegalStateException("Cannot generate unique invoice code");
        } while (invoiceRepo.existsByInvoiceCode(code));
        return code;
    }
}
