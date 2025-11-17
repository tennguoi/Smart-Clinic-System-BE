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
        // Validation: Ngày khám chỉ được đặt trong 3 ngày
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxDate = now.plusDays(3);
        
        if (req.appointmentTime().isBefore(now)) {
            throw new IllegalArgumentException("Ngày khám không được trong quá khứ");
        }
        
        if (req.appointmentTime().isAfter(maxDate)) {
            throw new IllegalArgumentException("Ngày khám chỉ được đặt trong vòng 3 ngày tới");
        }
        
        Appointment entity = Appointment.builder()
                .patientName(req.patientName())
                .age(req.age())
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

    public List<AppointmentResponse> listByStatus(Status status) {
        return repo.findByStatus(status).stream().map(this::toResponse).toList();
    }

    @Transactional
    public AppointmentResponse updateStatus(UUID id, Status status) {
        Appointment appt = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        appt.setStatus(status);
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User actionBy = userRepo.findByEmail(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Nếu status là CONFIRMED, lưu thông tin người xác nhận và gửi email
        if (status == Status.Confirmed && appt.getConfirmedBy() == null) {
            appt.setConfirmedBy(actionBy);
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
                        actionBy.getFullName()
                    );
                } catch (Exception e) {
                    // Log lỗi nhưng không throw để không ảnh hưởng đến việc xác nhận
                    System.err.println("Failed to send confirmation email: " + e.getMessage());
                }
            }
        }
        
        // Nếu status là CANCELLED, gửi email thông báo hủy
        if (status == Status.Cancelled && appt.getStatus() != Status.Cancelled) {
            if (appt.getEmail() != null && !appt.getEmail().isEmpty()) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    String formattedTime = appt.getAppointmentTime().format(formatter);
                    
                    emailService.sendAppointmentCancellation(
                        appt.getEmail(),
                        appt.getPatientName(),
                        appt.getAppointmentCode(),
                        formattedTime,
                        actionBy.getFullName()
                    );
                } catch (Exception e) {
                    // Log lỗi nhưng không throw để không ảnh hưởng đến việc hủy
                    System.err.println("Failed to send cancellation email: " + e.getMessage());
                }
            }
        }
        
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
                a.getAge(),
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
