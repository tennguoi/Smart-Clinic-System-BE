package com.example.demo.service;

import com.example.demo.dto.AppointmentRequest;
import com.example.demo.dto.AppointmentResponse;
import com.example.demo.entity.Appointment;
import com.example.demo.entity.Appointment.Status;
import com.example.demo.entity.User;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.CodeGenerator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppointmentService {
    private final AppointmentRepository repo;
    private final UserRepository userRepo;
    private final EmailService emailService;

    public AppointmentService(AppointmentRepository repo, UserRepository userRepo, EmailService emailService) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.emailService = emailService;
    }

    // Phương thức đặt lịch nhanh (giữ nguyên code cũ của bạn)
    @Transactional
    public AppointmentResponse quickBook(AppointmentRequest req) {
        Appointment entity = Appointment.builder()
                .patientName(req.patientName())
                .phone(req.phone())
                .email(req.email())
                .appointmentTime(req.appointmentTime())
                .notes(req.notes())
                .quickBook(true)
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

    // [MỚI] Phương thức tạo lịch hẹn từ form quản lý (Lễ tân)
    @Transactional
    public AppointmentResponse create(AppointmentRequest req) {
        Appointment entity = Appointment.builder()
                .patientName(req.patientName())
                .phone(req.phone())
                .email(req.email())
                .appointmentTime(req.appointmentTime())
                .notes(req.notes())
                .quickBook(false) // Đánh dấu đây không phải đặt nhanh
                .appointmentCode(generateUniqueCode())
                .build();
        // Status mặc định là Pending do @PrePersist trong Entity

        entity = repo.save(entity);
        return toResponse(entity);
    }

    // Phương thức cũ (giữ nguyên để tương thích ngược nếu cần)
    public List<AppointmentResponse> listByStatus(Status status) {
        return repo.findByStatus(status).stream().map(this::toResponse).toList();
    }

    // [MỚI & CẬP NHẬT] Lọc danh sách theo Trạng thái, SĐT và Tên bệnh nhân
    public List<AppointmentResponse> listBy(Status status, String phone, String patientName) {
        // 1. Lấy danh sách gốc theo trạng thái từ DB
        List<Appointment> results = repo.findByStatus(status);

        // 2. Lọc theo SĐT (nếu có) - Tìm tương đối (chứa chuỗi số)
        if (phone != null && !phone.isBlank()) {
            String searchPhone = phone.trim();
            results = results.stream()
                    .filter(a -> a.getPhone().contains(searchPhone))
                    .collect(Collectors.toList());
        }

        // 3. Lọc theo Tên bệnh nhân (nếu có) - Tìm tương đối, không phân biệt hoa thường
        if (patientName != null && !patientName.isBlank()) {
            String searchName = patientName.trim().toLowerCase();
            results = results.stream()
                    .filter(a -> a.getPatientName().toLowerCase().contains(searchName))
                    .collect(Collectors.toList());
        }

        // Chuyển đổi sang DTO và trả về
        return results.stream().map(this::toResponse).toList();
    }

    // Phương thức cập nhật trạng thái (Xác nhận/Hủy)
    @Transactional
    public AppointmentResponse updateStatus(UUID id, Status status) {
        Appointment appt = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        appt.setStatus(status);

        // Nếu xác nhận (Confirmed), lưu người xác nhận và gửi mail
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

    private AppointmentResponse toResponse(Appointment a) {
        String confirmedByName = null;
        if (a.getConfirmedBy() != null) {
            confirmedByName = a.getConfirmedBy().getFullName();
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