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

    // --- PHƯƠNG THỨC MỚI (1) ĐƯỢC THÊM VÀO ---
    // Dùng cho lễ tân tạo lịch hẹn từ form
    @Transactional
    public AppointmentResponse create(AppointmentRequest req) {
        Appointment entity = Appointment.builder()
                .patientName(req.patientName())
                .phone(req.phone())
                .email(req.email())
                .appointmentTime(req.appointmentTime())
                .notes(req.notes())
                .quickBook(false) // Lễ tân tạo, không phải "quick book"
                .appointmentCode(generateUniqueCode())
                .build();
        // Trạng thái sẽ tự động là 'Pending' do @PrePersist

        entity = repo.save(entity);
        return toResponse(entity);
    }
    // --- HẾT PHẦN THÊM MỚI (1) ---

    public List<AppointmentResponse> listByStatus(Status status) {
        return repo.findByStatus(status).stream().map(this::toResponse).toList();
    }

    // --- PHƯƠNG THỨC MỚI (2) ĐƯỢC THÊM VÀO ---
    // Dùng để lọc danh sách theo trạng thái VÀ số điện thoại
    public List<AppointmentResponse> listBy(Status status, String phone) {
        List<Appointment> results;

        if (phone != null && !phone.isBlank()) {
            // 1. Nếu có SĐT, lọc theo SĐT và trạng thái
            results = repo.findByPhoneAndStatus(phone, status);
        } else {
            // 2. Nếu không có SĐT, lọc như cũ (chỉ theo trạng thái)
            results = repo.findByStatus(status);
        }

        return results.stream().map(this::toResponse).toList();
    }
    // --- HẾT PHẦN THÊM MỚI (2) ---

    @Transactional
    public AppointmentResponse updateStatus(UUID id, Status status) {
        Appointment appt = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        appt.setStatus(status);

        // Nếu status là CONFIRMED, lưu thông tin người xác nhận và gửi email
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

            // Gửi email xác nhận nếu có email
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
                    // Log lỗi nhưng không throw để không ảnh hưởng đến việc xác nhận
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