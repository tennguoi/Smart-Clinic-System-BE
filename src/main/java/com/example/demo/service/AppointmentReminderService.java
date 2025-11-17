package com.example.demo.service;

import com.example.demo.entity.Appointment;
import com.example.demo.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentReminderService {

    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;

    // Chạy mỗi giờ để kiểm tra và gửi email nhắc nhở vào 20h tối
    @Scheduled(cron = "0 0 * * * ?") // Chạy mỗi giờ
    public void sendAppointmentReminders() {
        LocalDateTime now = LocalDateTime.now();
        
        // Chỉ gửi email vào 20h tối
        if (now.getHour() != 20) {
            return;
        }

        // Tìm các appointment vào ngày mai
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime startOfTomorrow = tomorrow.atStartOfDay();
        LocalDateTime endOfTomorrow = tomorrow.atTime(LocalTime.MAX);

        List<Appointment> appointments = appointmentRepository.findByStatus(Appointment.Status.Confirmed)
                .stream()
                .filter(apt -> {
                    LocalDateTime aptTime = apt.getAppointmentTime();
                    return aptTime.isAfter(startOfTomorrow.minusSeconds(1)) && 
                           aptTime.isBefore(endOfTomorrow.plusSeconds(1));
                })
                .toList();

        // Gửi email nhắc nhở cho mỗi appointment
        for (Appointment appointment : appointments) {
            if (appointment.getEmail() != null && !appointment.getEmail().isBlank()) {
                try {
                    emailService.sendAppointmentReminder(
                            appointment.getEmail(),
                            appointment.getPatientName(),
                            appointment.getAppointmentCode(),
                            appointment.getAppointmentTime()
                    );
                } catch (Exception e) {
                    System.err.println("Failed to send reminder email for appointment " + 
                                     appointment.getAppointmentCode() + ": " + e.getMessage());
                }
            }
        }
    }
}

