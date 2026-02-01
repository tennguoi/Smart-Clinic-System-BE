// src/main/java/com/example/demo/service/AppointmentService.java
package com.example.demo.service;

import com.example.demo.dto.appointment.AppointmentRequest;
import com.example.demo.dto.appointment.AppointmentResponse;
import com.example.demo.dto.ServiceResponse;
import com.example.demo.entity.Appointment;
import com.example.demo.entity.Patient;
import com.example.demo.entity.User;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.ServiceRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.CodeGenerator;
import com.example.demo.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@org.springframework.stereotype.Service
public class AppointmentService {

    private final AppointmentRepository repo;
    private final UserRepository userRepo;
    private final ServiceRepository serviceRepo;
    private final EmailService emailService;
    private final PatientRepository patientRepo;

    @Autowired
    public AppointmentService(AppointmentRepository repo, UserRepository userRepo, ServiceRepository serviceRepo,
            EmailService emailService, PatientRepository patientRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.serviceRepo = serviceRepo;
        this.emailService = emailService;
        this.patientRepo = patientRepo;
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

        final Appointment savedEntity = repo.save(entity);

        // Prepare service names for email
        List<String> serviceNames = prepareServiceNames(req.serviceIds());

        // Send email notification (async, won't block)
        sendEmailSafely(() -> {
            if (savedEntity.getEmail() != null && !savedEntity.getEmail().trim().isEmpty()) {
                String magicLink = "http://localhost:5173/appointments/tracking?code="
                        + savedEntity.getAppointmentCode();
                emailService.sendNewAppointmentNotification(
                        savedEntity.getEmail(),
                        savedEntity.getPatientName(),
                        savedEntity.getAppointmentCode(),
                        savedEntity.getAppointmentTime(),
                        savedEntity.getNotes(),
                        serviceNames,
                        magicLink);
                System.out.println("✓ Email sent to: " + savedEntity.getEmail());
            }
        }, "quickBook", savedEntity.getEmail());

        return toResponse(savedEntity);
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

        final Appointment savedEntity = repo.save(entity);

        // Prepare service names for email
        List<String> serviceNames = prepareServiceNames(req.serviceIds());

        // Send email notification (async, won't block)
        sendEmailSafely(() -> {
            if (savedEntity.getEmail() != null && !savedEntity.getEmail().trim().isEmpty()) {
                String magicLink = "http://localhost:5173/appointments/tracking?code="
                        + savedEntity.getAppointmentCode();
                emailService.sendNewAppointmentNotification(
                        savedEntity.getEmail(),
                        savedEntity.getPatientName(),
                        savedEntity.getAppointmentCode(),
                        savedEntity.getAppointmentTime(),
                        savedEntity.getNotes(),
                        serviceNames,
                        magicLink);
                System.out.println("✓ Email sent to: " + savedEntity.getEmail());
            }
        }, "create", savedEntity.getEmail());

        return toResponse(savedEntity);
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
                Sort.by("createdAt").descending());

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

        // Send email notification for status change (only email, no SMS)
        if (oldStatus != status && appt.getEmail() != null && !appt.getEmail().isEmpty()) {
            final Appointment finalAppt = appt;
            final String newStatusStr = status.toString();

            sendEmailSafely(() -> {
                String magicLink = "http://localhost:5173/appointments/tracking?code=" + finalAppt.getAppointmentCode();

                if ("Cancelled".equalsIgnoreCase(newStatusStr)) {
                    emailService.sendAppointmentCancellationNotification(
                            finalAppt.getEmail(),
                            finalAppt.getPatientName(),
                            finalAppt.getAppointmentCode(),
                            magicLink);
                } else if ("Confirmed".equalsIgnoreCase(newStatusStr)) {
                    // Send confirmation email with confirmer name
                    String confirmerName = finalAppt.getConfirmedBy() != null
                            ? finalAppt.getConfirmedBy().getFullName()
                            : "Hệ thống";
                    emailService.sendAppointmentConfirmation(
                            finalAppt.getEmail(),
                            finalAppt.getPatientName(),
                            finalAppt.getAppointmentCode(),
                            finalAppt.getAppointmentTime().toString(),
                            confirmerName);
                } else {
                    emailService.sendAppointmentStatusChangeNotification(
                            finalAppt.getEmail(),
                            finalAppt.getPatientName(),
                            finalAppt.getAppointmentCode(),
                            newStatusStr,
                            magicLink);
                }
                System.out.println("✓ Status change email sent to: " + finalAppt.getEmail());
            }, "updateStatus", appt.getEmail());
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

        appt.setPatientName(req.patientName());
        appt.setPhone(req.phone());
        appt.setEmail(req.email());
        appt.setAppointmentTime(req.appointmentTime());
        appt.setNotes(req.notes());
        appt.setServiceIds(joinServiceIds(req.serviceIds()));

        appt = repo.save(appt);

        // Prepare service names for email
        List<String> serviceNames = prepareServiceNames(req.serviceIds());

        // Send email notification (only email, no SMS)
        if (appt.getEmail() != null && !appt.getEmail().isEmpty()) {
            final Appointment finalAppt = appt;
            final LocalDateTime finalOldTime = oldTime;

            sendEmailSafely(() -> {
                String magicLink = "http://localhost:5173/appointments/tracking?code=" + finalAppt.getAppointmentCode();

                if (finalOldTime != null && !finalOldTime.isEqual(finalAppt.getAppointmentTime())) {
                    // Time changed - send update notification
                    emailService.sendAppointmentUpdateNotification(
                            finalAppt.getEmail(),
                            finalAppt.getPatientName(),
                            finalAppt.getAppointmentCode(),
                            finalOldTime,
                            finalAppt.getAppointmentTime(),
                            magicLink);
                } else {
                    // General update - send new appointment notification
                    emailService.sendNewAppointmentNotification(
                            finalAppt.getEmail(),
                            finalAppt.getPatientName(),
                            finalAppt.getAppointmentCode(),
                            finalAppt.getAppointmentTime(),
                            finalAppt.getNotes(),
                            serviceNames,
                            magicLink);
                }
                System.out.println("✓ Update email sent to: " + finalAppt.getEmail());
            }, "updateInfo", finalAppt.getEmail());
        }

        return toResponse(appt);
    }

    // =========================================================
    // 7. HELPERS
    // =========================================================

    /**
     * Safe email sending wrapper - catches all exceptions to prevent 500 errors
     */
    private void sendEmailSafely(Runnable emailTask, String operation, String email) {
        try {
            emailTask.run();
        } catch (Exception e) {
            // Log error but don't throw - email failure shouldn't break the operation
            System.err.println("⚠ Email sending failed for " + operation + " (email: " + email + ")");
            System.err.println("  Error: " + e.getMessage());
            // Don't rethrow - allow the main operation to succeed
        }
    }

    private String joinServiceIds(List<String> ids) {
        if (ids == null || ids.isEmpty())
            return null;
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
                                s.getPhotoUrl()))
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
                confirmedByName);
    }

    private String generateUniqueCode() {
        String code;
        int attempts = 0;
        do {
            code = CodeGenerator.generate(8);
            attempts++;
            if (attempts > 100)
                throw new IllegalStateException("Cannot generate unique code");
        } while (repo.findByAppointmentCode(code).isPresent());
        return code;
    }

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
                                appointment.getAppointmentTime());
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
