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
import com.example.demo.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.LocalDate;
@org.springframework.stereotype.Service

public class AppointmentService {

    private final AppointmentRepository repo;
    private final UserRepository userRepo;
    private final ServiceRepository serviceRepo;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final PatientRepository patientRepo;
    private final com.example.demo.repository.InvoiceRepository invoiceRepo;
    private final com.example.demo.repository.InvoiceDetailRepository invoiceDetailRepo;

    @Autowired
    public AppointmentService(AppointmentRepository repo, UserRepository userRepo, ServiceRepository serviceRepo, 
                              EmailService emailService, NotificationService notificationService,
                              PatientRepository patientRepo, com.example.demo.repository.InvoiceRepository invoiceRepo, 
                              com.example.demo.repository.InvoiceDetailRepository invoiceDetailRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.serviceRepo = serviceRepo;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.patientRepo = patientRepo;
        this.invoiceRepo = invoiceRepo;
        this.invoiceDetailRepo = invoiceDetailRepo;
    }

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

        // Prepare service names for notifications
        List<String> serviceNames = prepareServiceNames(req.serviceIds());

        System.out.println("========== QUICK BOOK - PREPARING NOTIFICATIONS ==========");
        System.out.println("Phone: " + entity.getPhone());
        System.out.println("Email: " + entity.getEmail());
        
        // ALWAYS send SMS notification if phone is provided - THIS IS MANDATORY
        boolean smsSent = false;
        if (entity.getPhone() != null && !entity.getPhone().trim().isEmpty()) {
            try {
                System.out.println(">>> ATTEMPTING to send SMS to: " + entity.getPhone());
                notificationService.sendBookingSms(entity, serviceNames);
                smsSent = true;
                System.out.println("✓ SMS notification sent SUCCESSFULLY to: " + entity.getPhone());
            } catch (Exception e) {
                System.err.println("✗ FAILED to send SMS notification to: " + entity.getPhone());
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
                // Continue despite SMS failure - don't block the booking
            }
        } else {
            System.out.println("⚠ Phone number not provided - SKIPPING SMS notification");
        }
        
        // Send email notification if email is provided
        boolean emailSent = false;
        if (entity.getEmail() != null && !entity.getEmail().trim().isEmpty()) {
            try {
                System.out.println(">>> Attempting to send email to: " + entity.getEmail());
                notificationService.sendBookingEmail(entity, serviceNames);
                emailSent = true;
                System.out.println("✓ Email notification sent successfully to: " + entity.getEmail());
            } catch (Exception e) {
                System.err.println("✗ Failed to send email notification to: " + entity.getEmail());
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠ Email not provided - skipping email notification");
        }
        
        System.out.println("========== NOTIFICATION SUMMARY ==========");
        System.out.println("SMS: " + (smsSent ? "✓ SENT" : "✗ NOT SENT"));
        System.out.println("Email: " + (emailSent ? "✓ SENT" : "✗ NOT SENT"));
        System.out.println("==========================================");

        return toResponse(entity);
    }

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

        // Prepare service names for notifications
        List<String> serviceNames = prepareServiceNames(req.serviceIds());
        
        System.out.println("========== CREATE APPOINTMENT - SENDING NOTIFICATIONS ==========");
        System.out.println("Appointment Code: " + entity.getAppointmentCode());
        System.out.println("Phone: " + entity.getPhone());
        System.out.println("Email: " + entity.getEmail());
        
        // ALWAYS send SMS notification if phone is provided - THIS IS MANDATORY
        boolean smsSent = false;
        if (entity.getPhone() != null && !entity.getPhone().trim().isEmpty()) {
            try {
                System.out.println(">>> ATTEMPTING to send SMS to: " + entity.getPhone());
                notificationService.sendBookingSms(entity, serviceNames);
                smsSent = true;
                System.out.println("✓ SMS notification sent SUCCESSFULLY to: " + entity.getPhone());
            } catch (Exception e) {
                System.err.println("✗ FAILED to send SMS notification to: " + entity.getPhone());
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
                // Continue despite SMS failure - don't block the booking
            }
        } else {
            System.out.println("⚠ Phone number not provided - SKIPPING SMS notification");
        }
        
        // Send email notification if email is provided
        boolean emailSent = false;
        if (entity.getEmail() != null && !entity.getEmail().trim().isEmpty()) {
            try {
                System.out.println(">>> Attempting to send email to: " + entity.getEmail());
                notificationService.sendBookingEmail(entity, serviceNames);
                emailSent = true;
                System.out.println("✓ Email notification sent successfully to: " + entity.getEmail());
            } catch (Exception e) {
                System.err.println("✗ Failed to send email notification to: " + entity.getEmail());
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠ Email not provided - skipping email notification");
        }
        
        System.out.println("========== NOTIFICATION SUMMARY ==========");
        System.out.println("SMS: " + (smsSent ? "✓ SENT" : "✗ NOT SENT"));
        System.out.println("Email: " + (emailSent ? "✓ SENT" : "✗ NOT SENT"));
        System.out.println("==========================================");

        return toResponse(entity);
    }

    // Phương thức cũ (giữ nguyên)
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

        Appointment.Status oldStatus = appt.getStatus();
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
        }
        
        appt = repo.save(appt);

        // Send notification for status change (includes SMS via notifyStatusChange)
        if (oldStatus != status) {
            System.out.println("========== STATUS CHANGE NOTIFICATION ==========");
            System.out.println("Old Status: " + oldStatus + " -> New Status: " + status);
            System.out.println("Phone: " + appt.getPhone());
            System.out.println("Email: " + appt.getEmail());
            
            // Send SMS immediately
            boolean smsSent = false;
            if (appt.getPhone() != null && !appt.getPhone().isEmpty()) {
                try {
                    System.out.println(">>> ATTEMPTING to send SMS notification for status change");
                    String magicLink = "http://localhost:5173/appointments/tracking?code=" + appt.getAppointmentCode();
                    String smsContent = "Trang thai lich kham thay doi! Ma: " + appt.getAppointmentCode() + 
                                      ". Trang thai moi: " + status + ". Chi tiet: " + magicLink;
                    // Direct SMS send for immediate delivery
                    notificationService.sendBookingSms(appt, new ArrayList<>());
                    smsSent = true;
                    System.out.println("✓ SMS notification sent SUCCESSFULLY");
                } catch (Exception e) {
                    System.err.println("✗ FAILED to send SMS notification");
                    System.err.println("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Send email (delayed by 30s in notifyStatusChange)
            boolean emailScheduled = false;
            if (appt.getEmail() != null && !appt.getEmail().isEmpty()) {
                try {
                    System.out.println(">>> Scheduling email notification (30s delay)");
                    notificationService.notifyStatusChange(appt, oldStatus.toString(), status.toString());
                    emailScheduled = true;
                    System.out.println("✓ Email notification scheduled successfully");
                } catch (Exception e) {
                    System.err.println("✗ Failed to schedule email notification");
                    System.err.println("Error: " + e.getMessage());
                }
            }
            
            System.out.println("========== NOTIFICATION SUMMARY ==========");
            System.out.println("SMS: " + (smsSent ? "✓ SENT" : "✗ NOT SENT"));
            System.out.println("Email: " + (emailScheduled ? "✓ SCHEDULED" : "✗ NOT SCHEDULED"));
            System.out.println("==========================================");
        }
        
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

        LocalDateTime oldTime = appt.getAppointmentTime();
        String oldPhone = appt.getPhone();
        String oldEmail = appt.getEmail();

        appt.setPatientName(req.patientName());
        appt.setPhone(req.phone());
        appt.setEmail(req.email());
        appt.setAppointmentTime(req.appointmentTime());
        appt.setNotes(req.notes());
        appt.setServiceIds(joinServiceIds(req.serviceIds()));

        appt = repo.save(appt);

        // Prepare service names for notifications
        List<String> serviceNames = prepareServiceNames(req.serviceIds());

        System.out.println("========== UPDATE APPOINTMENT - SENDING NOTIFICATIONS ==========");
        System.out.println("Appointment Code: " + appt.getAppointmentCode());
        System.out.println("Old Phone: " + oldPhone + " -> New Phone: " + appt.getPhone());
        System.out.println("Old Email: " + oldEmail + " -> New Email: " + appt.getEmail());
        System.out.println("Old Time: " + oldTime + " -> New Time: " + appt.getAppointmentTime());

        // ALWAYS send SMS when updating - THIS IS MANDATORY
        boolean smsSent = false;
        if (appt.getPhone() != null && !appt.getPhone().trim().isEmpty()) {
            try {
                System.out.println(">>> ATTEMPTING to send SMS update notification to: " + appt.getPhone());
                
                // Check if time changed to send appropriate message
                if (oldTime != null && !oldTime.isEqual(req.appointmentTime())) {
                    // Send time change notification
                    notificationService.notifyAppointmentChange(appt, oldTime, appt.getAppointmentTime());
                } else {
                    // Send general update notification
                    notificationService.sendBookingSms(appt, serviceNames);
                }
                
                smsSent = true;
                System.out.println("✓ SMS update notification sent SUCCESSFULLY to: " + appt.getPhone());
            } catch (Exception e) {
                System.err.println("✗ FAILED to send SMS update notification to: " + appt.getPhone());
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠ Phone number not provided - SKIPPING SMS notification");
        }

        // Send email notification if time changed or email is new
        boolean emailSent = false;
        if (appt.getEmail() != null && !appt.getEmail().isEmpty()) {
            try {
                if (oldTime != null && !oldTime.isEqual(req.appointmentTime())) {
                    System.out.println(">>> Sending email for time change (30s delay)");
                    notificationService.notifyAppointmentChange(appt, oldTime, appt.getAppointmentTime());
                    emailSent = true;
                    System.out.println("✓ Email notification scheduled successfully");
                } else {
                    // Send update email even if time didn't change
                    System.out.println(">>> Sending email for general update");
                    notificationService.sendBookingEmail(appt, serviceNames);
                    emailSent = true;
                    System.out.println("✓ Email notification sent successfully");
                }
            } catch (Exception e) {
                System.err.println("✗ Failed to send email notification");
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠ Email not provided - skipping email notification");
        }

        System.out.println("========== NOTIFICATION SUMMARY ==========");
        System.out.println("SMS: " + (smsSent ? "✓ SENT" : "✗ NOT SENT"));
        System.out.println("Email: " + (emailSent ? "✓ SENT/SCHEDULED" : "✗ NOT SENT"));
        System.out.println("==========================================");

        return toResponse(appt);
    }

    // =========================================================
    // 7. HELPERS
    // =========================================================

    private String joinServiceIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) return null;
        return String.join(",", ids);
    }
    
    private List<String> prepareServiceNames(List<String> serviceIds) {
        List<String> serviceNames = new ArrayList<>();
        if (serviceIds != null && !serviceIds.isEmpty()) {
            serviceNames = serviceRepo.findAllById(serviceIds)
                    .stream()
                    .map(com.example.demo.entity.Service::getName)
                    .collect(Collectors.toList());
        }
        return serviceNames;
    }

    public AppointmentResponse toResponse(Appointment a) {
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

    //@Scheduled(cron = "0 0 20 * * ?")  
    //@Scheduled(cron = "0/30 * * * * ?")
    @Transactional
    public void sendDailyReminderEmails() {
        try {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            
            // Sử dụng phương thức mới để tối ưu truy vấn
            List<Appointment> tomorrowAppointments = repo.findByStatusAndAppointmentTimeDate(
                Appointment.Status.Confirmed, tomorrow);
            
            int sentCount = 0;
            int failedCount = 0;
            
            for (Appointment appointment : tomorrowAppointments) {
                if (appointment.getEmail() != null && !appointment.getEmail().trim().isEmpty()) {
                    try {
                        emailService.sendAppointmentReminder(
                                appointment.getEmail(),
                                appointment.getPatientName(),
                                appointment.getAppointmentCode(),
                                appointment.getAppointmentTime()
                        );
                        sentCount++;
                        
                        System.out.println("Đã gửi email nhắc nhở cho: " + appointment.getPatientName() + 
                                         " - Email: " + appointment.getEmail() + 
                                         " - Thời gian: " + appointment.getAppointmentTime());
                        
                    } catch (Exception e) {
                        failedCount++;
                        System.err.println("Lỗi gửi email nhắc nhở cho: " + appointment.getPatientName() + 
                                         " - Lỗi: " + e.getMessage());
                    }
                }
            }
            
            System.out.println("Kết quả gửi email nhắc nhở: " + 
                             "Thành công: " + sentCount + ", Thất bại: " + failedCount +
                             ", Tổng lịch hẹn: " + tomorrowAppointments.size());
            
        } catch (Exception e) {
            System.err.println("Lỗi trong quá trình gửi email nhắc nhở hàng loạt: " + e.getMessage());
        }
    }
}