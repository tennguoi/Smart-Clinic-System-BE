package com.example.demo.service;

import com.example.demo.dto.AppointmentRequest;
import com.example.demo.dto.AppointmentResponse;
import com.example.demo.dto.ServiceResponse;
import com.example.demo.entity.Appointment;
import com.example.demo.entity.Appointment.Status;
import com.example.demo.entity.Service;
import com.example.demo.entity.User;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.ServiceRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.CodeGenerator;
import com.example.demo.repository.PatientRepository;
import org.springframework.security.core.context.SecurityContextHolder;


import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class AppointmentService {
    private final AppointmentRepository repo;
    private final UserRepository userRepo;
    private final ServiceRepository serviceRepo;
    private final EmailService emailService;
    private final PatientRepository patientRepo;
    private final com.example.demo.repository.InvoiceRepository invoiceRepo;
    private final com.example.demo.repository.InvoiceDetailRepository invoiceDetailRepo;

    public AppointmentService(AppointmentRepository repo, UserRepository userRepo, ServiceRepository serviceRepo, EmailService emailService,
                              PatientRepository patientRepo, com.example.demo.repository.InvoiceRepository invoiceRepo, com.example.demo.repository.InvoiceDetailRepository invoiceDetailRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.serviceRepo = serviceRepo;
        this.emailService = emailService;
        this.patientRepo = patientRepo;
        this.invoiceRepo = invoiceRepo;
        this.invoiceDetailRepo = invoiceDetailRepo;
    }

    // Phương thức đặt lịch nhanh (Khách đặt)
    @Transactional
    public AppointmentResponse quickBook(AppointmentRequest req) {
        // Kiểm tra thời gian: Không được đặt quá khứ
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
            .serviceIds(req.serviceIds() != null ? String.join(",", req.serviceIds()) : null)
                .appointmentCode(generateUniqueCode())
                .build();
        entity = repo.save(entity);

        if (entity.getEmail() != null && !entity.getEmail().isBlank()) {
            try {
                emailService.sendAppointmentPendingNotification(
                        entity.getEmail(),
                        entity.getPatientName(),
                        entity.getAppointmentCode(),
                        entity.getAppointmentTime()
                );
            } catch (Exception e) {
                System.err.println("Failed to send pending appointment email: " + e.getMessage());
            }
        }

        // [NEW] Create Draft Invoice for Stats
        if (req.serviceIds() != null && !req.serviceIds().isEmpty()) {
            try {
                com.example.demo.entity.Patient patient = findOrCreatePatient(req);
                createDraftInvoice(patient, req.serviceIds());
            } catch (Exception e) {
                System.err.println("Failed to create draft invoice: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return toResponse(entity);
    }

    // Phương thức tạo lịch hẹn từ form quản lý (Lễ tân)
    @Transactional
    public AppointmentResponse create(AppointmentRequest req) {
        // Kiểm tra thời gian: Không được đặt quá khứ
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
            .serviceIds(req.serviceIds() != null ? String.join(",", req.serviceIds()) : null)
                .appointmentCode(generateUniqueCode())
                .build();

        entity = repo.save(entity);

        // [NEW] Create Draft Invoice for Stats
        if (req.serviceIds() != null && !req.serviceIds().isEmpty()) {
            try {
                com.example.demo.entity.Patient patient = findOrCreatePatient(req);
                createDraftInvoice(patient, req.serviceIds());
            } catch (Exception e) {
                System.err.println("Failed to create draft invoice: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return toResponse(entity);
    }

    // Phương thức cũ (giữ nguyên)
    public List<AppointmentResponse> listByStatus(Status status) {
        return repo.findByStatus(status).stream().map(this::toResponse).toList();
    }

    // [CẬP NHẬT QUAN TRỌNG] Lọc danh sách & SẮP XẾP MỚI NHẤT LÊN ĐẦU
    public List<AppointmentResponse> listBy(Status status, String phone, String patientName) {
        List<Appointment> results = repo.findByStatus(status);

        // 1. Lọc theo SĐT
        if (phone != null && !phone.isBlank()) {
            String searchPhone = phone.trim();
            results = results.stream()
                    .filter(a -> a.getPhone().contains(searchPhone))
                    .collect(Collectors.toList());
        }

        // 2. Lọc theo Tên
        if (patientName != null && !patientName.isBlank()) {
            String searchName = patientName.trim().toLowerCase();
            results = results.stream()
                    .filter(a -> a.getPatientName().toLowerCase().contains(searchName))
                    .collect(Collectors.toList());
        }

        // 3. [MỚI] Sắp xếp và trả về (Newest First)
        return results.stream()
                // Logic sắp xếp: So sánh ngày tạo, cái nào lớn hơn (mới hơn) thì đứng trước
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .map(this::toResponse)
                .toList();
    }

    // Phương thức cập nhật trạng thái
    @Transactional
    public AppointmentResponse updateStatus(UUID id, Status status) {
        Appointment appt = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        appt.setStatus(status);

        if (status == Status.Confirmed && appt.getConfirmedBy() == null) {
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
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    String formattedTime = appt.getAppointmentTime().format(formatter);

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

    // Chức năng sửa thông tin lịch hẹn
    @Transactional
    public AppointmentResponse updateInfo(UUID id, AppointmentRequest req) {
        // Kiểm tra thời gian: Không được dời lịch về quá khứ
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

        appt = repo.save(appt);

        return toResponse(appt);
    }

    private AppointmentResponse toResponse(Appointment a) {
        String confirmedByName = null;
        if (a.getConfirmedBy() != null) {
            confirmedByName = a.getConfirmedBy().getFullName();
        }

        java.util.List<String> serviceIdsList = null;
        java.util.List<ServiceResponse> serviceResponses = new java.util.ArrayList<>();

        if (a.getServiceIds() != null && !a.getServiceIds().isBlank()) {
            serviceIdsList = java.util.Arrays.stream(a.getServiceIds().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            // Fetch services details
            if (serviceIdsList != null && !serviceIdsList.isEmpty()) {
                List<Service> services = serviceRepo.findAllById(serviceIdsList);
                serviceResponses = services.stream()
                        .map(s -> new ServiceResponse(
                                s.getServiceId(),
                                s.getName(),
                                s.getDescription(),
                                s.getCategory().name(),
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

    private com.example.demo.entity.Patient findOrCreatePatient(AppointmentRequest req) {
        return patientRepo.findByPhone(req.phone())
                .orElseGet(() -> {
                    com.example.demo.entity.Patient newPatient = com.example.demo.entity.Patient.builder()
                            .fullName(req.patientName())
                            .phone(req.phone())
                            .email(req.email())
                            .dob(java.time.LocalDate.of(2000, 1, 1)) // Default DOB
                            .gender(com.example.demo.entity.Patient.Gender.other) // Default Gender
                            .address("Unknown")
                            .build();
                    return patientRepo.save(newPatient);
                });
    }

    private void createDraftInvoice(com.example.demo.entity.Patient patient, List<String> serviceIds) {
        if (serviceIds == null || serviceIds.isEmpty()) return;

        List<Service> services = serviceRepo.findAllById(serviceIds);
        if (services.isEmpty()) return;

        // Find a system user (Admin) to assign as creator
        // In a real app, this should be the logged in user or a system bot
        User systemUser = userRepo.findAll().stream().findFirst().orElseThrow(() -> new IllegalStateException("No users found"));

        // Create Invoice
        com.example.demo.entity.Invoice invoice = com.example.demo.entity.Invoice.builder()
                .invoiceCode(generateUniqueInvoiceCode())
                .patient(patient)
                .createdBy(systemUser)
                .status(com.example.demo.enums.InvoiceStatus.DRAFT)
                .totalAmount(java.math.BigDecimal.ZERO)
                .build();
        
        // Calculate total & details
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        List<com.example.demo.entity.InvoiceDetail> details = new java.util.ArrayList<>();

        for (Service s : services) {
            com.example.demo.entity.InvoiceDetail detail = com.example.demo.entity.InvoiceDetail.builder()
                    .invoice(invoice)
                    .service(s)
                    .quantity(1)
                    .unitPrice(s.getPrice())
                    .amount(s.getPrice())
                    .build();
            details.add(detail);
            total = total.add(s.getPrice());
        }

        invoice.setTotalAmount(total);
        invoice.setRemainingAmount(total);
        invoice.setDetails(details);

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
