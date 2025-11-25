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

@Service
public class AppointmentService {

    private final AppointmentRepository repo;
    private final UserRepository userRepo;
    private final ServiceRepository serviceRepo;
    private final EmailService emailService;
    private final PatientRepository patientRepo;
    private final com.example.demo.repository.InvoiceRepository invoiceRepo;
    private final com.example.demo.repository.InvoiceDetailRepository invoiceDetailRepo;

    @Autowired
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

    // Send email notification for new appointment
    if (entity.getEmail() != null && !entity.getEmail().trim().isEmpty()) {
        try {
            List<String> serviceNames = new ArrayList<>();
            if (req.serviceIds() != null && !req.serviceIds().isEmpty()) {
                serviceNames = serviceRepo.findAllById(req.serviceIds())
                        .stream()
                        .map(Service::getName)
                        .collect(Collectors.toList());
            }
            
            emailService.sendNewAppointmentNotification(
                entity.getEmail(),
                entity.getPatientName(),
                entity.getAppointmentCode(),
                entity.getAppointmentTime(),
                entity.getNotes(),
                serviceNames
            );
        } catch (Exception e) {
            System.err.println("Failed to send new appointment email: " + e.getMessage());
            e.printStackTrace();
        }
    }

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

    // Send email notification for new appointment
    if (entity.getEmail() != null && !entity.getEmail().trim().isEmpty()) {
        try {
            List<String> serviceNames = new ArrayList<>();
            if (req.serviceIds() != null && !req.serviceIds().isEmpty()) {
                serviceNames = serviceRepo.findAllById(req.serviceIds())
                        .stream()
                        .map(Service::getName)
                        .collect(Collectors.toList());
            }
            
            emailService.sendNewAppointmentNotification(
                entity.getEmail(),
                entity.getPatientName(),
                entity.getAppointmentCode(),
                entity.getAppointmentTime(),
                entity.getNotes(),
                serviceNames
            );
        } catch (Exception e) {
            System.err.println("Failed to send new appointment email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    return toResponse(entity);
}

    // Phương thức cũ (giữ nguyên)
    public List<AppointmentResponse> listByStatus(Status status) {
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

    //@Scheduled(cron = "0 0 20 * * ?")  
@Scheduled(cron = "0 */1 * * * ?")
@Transactional
public void sendDailyReminderEmails() {
    try {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        
        List<Appointment> tomorrowAppointments = repo.findByStatus(Status.Confirmed)
                .stream()
                .filter(appointment -> {
                    LocalDateTime appointmentTime = appointment.getAppointmentTime();
                    return appointmentTime != null && 
                           appointmentTime.toLocalDate().equals(tomorrow);
                })
                .collect(Collectors.toList());
        
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
                    
                    // Ghi log cho mỗi email gửi thành công
                    System.out.println("Đã gửi email nhắc nhở cho: " + appointment.getPatientName() + 
                                     " - Email: " + appointment.getEmail() + 
                                     " - Thời gian: " + appointment.getAppointmentTime());
                    
                } catch (Exception e) {
                    failedCount++;
                    System.err.println("Lỗi gửi email nhắc nhở cho: " + appointment.getPatientName() + 
                                     " - Lỗi: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
        
    } catch (Exception e) {
        System.err.println("Lỗi trong quá trình gửi email nhắc nhở hàng loạt: " + e.getMessage());
        e.printStackTrace();
    }
}
}
