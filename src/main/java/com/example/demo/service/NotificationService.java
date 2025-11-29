package com.example.demo.service;

import com.example.demo.entity.Appointment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final SmsService smsService;
    private final EmailService emailService;

    @Autowired
    public NotificationService(SmsService smsService, EmailService emailService) {
        this.smsService = smsService;
        this.emailService = emailService;
    }

    /**
     * Notify when booking is successful: SMS + Email immediately.
     */
    public void notifyBookingSuccess(Appointment appointment, List<String> serviceNames) {
        System.out.println("========== NOTIFY BOOKING SUCCESS CALLED ==========");
        System.out.println("Appointment Code: " + appointment.getAppointmentCode());
        System.out.println("Email: " + appointment.getEmail());
        System.out.println("Phone: " + appointment.getPhone());
        
        String magicLink = "http://localhost:5173/appointments/tracking?code=" + appointment.getAppointmentCode();
        
        // SMS
        String smsContent = "Dat lich thanh cong! Ma: " + appointment.getAppointmentCode() + 
                          ". Xem chi tiet: " + magicLink;
        smsService.sendSms(appointment.getPhone(), smsContent);

        // Email
        try {
            emailService.sendNewAppointmentNotification(
                appointment.getEmail(),
                appointment.getPatientName(),
                appointment.getAppointmentCode(),
                appointment.getAppointmentTime(),
                appointment.getNotes(),
                serviceNames,
                magicLink
            );
            System.out.println("Email sent successfully!");
        } catch (Exception e) {
            System.err.println("ERROR sending email: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("===================================================");
    }

    /**
     * Notify when appointment changes: SMS immediately, Email after 30s.
     */
    public void notifyAppointmentChange(Appointment appointment, LocalDateTime oldTime, LocalDateTime newTime) {
        String magicLink = "http://localhost:5173/appointments/tracking?code=" + appointment.getAppointmentCode();

        // SMS Immediate
        String smsContent = "Lich kham thay doi! Ma: " + appointment.getAppointmentCode() + 
                          ". Gio moi: " + newTime + ". Chi tiet: " + magicLink;
        smsService.sendSms(appointment.getPhone(), smsContent);

        // Email Delayed
        sendDelayedEmail(appointment, oldTime, newTime, magicLink);
    }

    /**
     * Notify when appointment status changes: SMS immediately, Email after 30s.
     */
    public void notifyStatusChange(Appointment appointment, String oldStatus, String newStatus) {
        System.out.println("========== NOTIFY STATUS CHANGE CALLED ==========");
        System.out.println("Appointment Code: " + appointment.getAppointmentCode());
        System.out.println("Old Status: " + oldStatus + " -> New Status: " + newStatus);
        System.out.println("Email: " + appointment.getEmail());
        System.out.println("Phone: " + appointment.getPhone());
        
        String magicLink = "http://localhost:5173/appointments/tracking?code=" + appointment.getAppointmentCode();

        // SMS Immediate
        String smsContent = "Trang thai lich kham thay doi! Ma: " + appointment.getAppointmentCode() + 
                          ". Trang thai moi: " + newStatus + ". Chi tiet: " + magicLink;
        smsService.sendSms(appointment.getPhone(), smsContent);

        // Email Delayed
        System.out.println("Scheduling delayed email in 30 seconds...");
        sendDelayedStatusEmail(appointment, newStatus, magicLink);
        System.out.println("=================================================");
    }

    @Async
    public void sendDelayedEmail(Appointment appointment, LocalDateTime oldTime, LocalDateTime newTime, String magicLink) {
        try {
            Thread.sleep(30000); // Wait 30 seconds
            emailService.sendAppointmentUpdateNotification(
                appointment.getEmail(),
                appointment.getPatientName(),
                appointment.getAppointmentCode(),
                oldTime,
                newTime,
                magicLink
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Delayed email interrupted: " + e.getMessage());
        }
    }

    @Async
    public void sendDelayedStatusEmail(Appointment appointment, String newStatus, String magicLink) {
        try {
            Thread.sleep(30000); // Wait 30 seconds
            
            if ("Cancelled".equalsIgnoreCase(newStatus)) {
                emailService.sendAppointmentCancellationNotification(
                    appointment.getEmail(),
                    appointment.getPatientName(),
                    appointment.getAppointmentCode(),
                    magicLink
                );
            } else if ("Confirmed".equalsIgnoreCase(newStatus)) {
                 // For Confirmed, we might want to use the specific confirmation email, 
                 // but that one requires 'confirmedBy'. 
                 // For now, let's use the generic status change or the confirmation one if we have info.
                 // But sendAppointmentConfirmation needs 'confirmedByName'.
                 // Let's use sendAppointmentStatusChangeNotification for simplicity unless it's a new booking confirmation which is handled by notifyBookingSuccess.
                 // Actually, updateStatus is often used for confirmation.
                 // Let's use sendAppointmentStatusChangeNotification for now to be safe and consistent.
                 emailService.sendAppointmentStatusChangeNotification(
                    appointment.getEmail(),
                    appointment.getPatientName(),
                    appointment.getAppointmentCode(),
                    newStatus,
                    magicLink
                );
            } else {
                emailService.sendAppointmentStatusChangeNotification(
                    appointment.getEmail(),
                    appointment.getPatientName(),
                    appointment.getAppointmentCode(),
                    newStatus,
                    magicLink
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Delayed email interrupted: " + e.getMessage());
        }
    }
    
    /**
     * Send booking email notification only
     */
    public void sendBookingEmail(Appointment appointment, List<String> serviceNames) {
        System.out.println("========== SENDING BOOKING EMAIL ==========");
        System.out.println("Appointment Code: " + appointment.getAppointmentCode());
        System.out.println("Email: " + appointment.getEmail());
        
        String magicLink = "http://localhost:5173/appointments/tracking?code=" + appointment.getAppointmentCode();
        
        try {
            emailService.sendNewAppointmentNotification(
                appointment.getEmail(),
                appointment.getPatientName(),
                appointment.getAppointmentCode(),
                appointment.getAppointmentTime(),
                appointment.getNotes(),
                serviceNames,
                magicLink
            );
            System.out.println("Email sent successfully!");
        } catch (Exception e) {
            System.err.println("ERROR sending email: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to be handled by the caller
        }
        System.out.println("========================================");
    }
    
    /**
     * Send booking SMS notification only
     */
    public void sendBookingSms(Appointment appointment, List<String> serviceNames) {
        System.out.println("========== SENDING BOOKING SMS ==========");
        System.out.println("Appointment Code: " + appointment.getAppointmentCode());
        System.out.println("Phone: " + appointment.getPhone());
        
        String magicLink = "http://localhost:5173/appointments/tracking?code=" + appointment.getAppointmentCode();
        
        // SMS
        String smsContent = "Dat lich thanh cong! Ma: " + appointment.getAppointmentCode() + 
                          ". Xem chi tiet: " + magicLink;
        smsService.sendSms(appointment.getPhone(), smsContent);
        
        System.out.println("SMS sent successfully!");
        System.out.println("========================================");
    }
}
