package com.example.demo.service;

import com.example.demo.dto.appointment.AppointmentRequest;
import com.example.demo.dto.appointment.AppointmentResponse;
import com.example.demo.dto.ServiceResponse;
import com.example.demo.entity.Appointment;
import com.example.demo.entity.Appointment.Status;
import com.example.demo.entity.Service;
import com.example.demo.entity.User;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.ServiceRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.CodeGenerator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
public class AppointmentService {

    private final AppointmentRepository repo;
    private final UserRepository userRepo;
    private final ServiceRepository serviceRepo;
    private final EmailService emailService;

    public AppointmentService(AppointmentRepository repo,
                              UserRepository userRepo,
                              ServiceRepository serviceRepo,
                              EmailService emailService) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.serviceRepo = serviceRepo;
        this.emailService = emailService;
    }

    // Phương thức đặt lịch nhanh (Khách đặt)
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
        return toResponse(entity);
    }

    // Phương thức tạo lịch hẹn từ form quản lý (Lễ tân)
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
        return toResponse(entity);
    }

    // Phương thức cũ (giữ nguyên)
    public List<AppointmentResponse> listByStatus(Status status) {
        return repo.findByStatus(status).stream()
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .map(this::toResponse)
                .toList();
    }

    // Lọc danh sách & sắp xếp mới nhất lên đầu
    public List<AppointmentResponse> listBy(Status status, String phone, String patientName) {
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

    // Phương thức cập nhật trạng thái
    @Transactional
    public AppointmentResponse updateStatus(UUID id, Status status) {
        Appointment appt = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

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

    // Chức năng sửa thông tin lịch hẹn (bây giờ cho sửa luôn dịch vụ)
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
        appt.setServiceIds(joinServiceIds(req.serviceIds())); // ← ĐÃ THÊM DÒNG NÀY

        appt = repo.save(appt);
        return toResponse(appt);
    }

    // Helper: chuyển List<String> → "id1,id2,id3"
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
                List<Service> services = serviceRepo.findAllById(serviceIdsList);
                serviceResponses = services.stream()
                        .map(s -> new ServiceResponse(
                                s.getServiceId(),
                                s.getName(),
                                s.getDescription(),
                                s.getCategory().getDisplayName(), // ← tiếng Việt đẹp
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
}