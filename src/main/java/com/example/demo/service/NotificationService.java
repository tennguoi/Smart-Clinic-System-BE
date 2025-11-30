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
        
        // SMS - ALWAYS try to send if phone exists
        if (appointment.getPhone() != null && !appointment.getPhone().trim().isEmpty()) {
            try {
                String smsContent = "Dat lich thanh cong! Ma: " + appointment.getAppointmentCode() + 
                                  ". Xem chi tiet: " + magicLink;
                smsService.sendSms(appointment.getPhone(), smsContent);
                System.out.println("✓ SMS sent successfully to: " + appointment.getPhone());
            } catch (Exception e) {
                System.err.println("✗ ERROR sending SMS to " + appointment.getPhone() + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠ No phone number - skipping SMS");
        }

        // Email
        if (appointment.getEmail() != null && !appointment.getEmail().trim().isEmpty()) {
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
                System.out.println("✓ Email sent successfully to: " + appointment.getEmail());
            } catch (Exception e) {
                System.err.println("✗ ERROR sending email to " + appointment.getEmail() + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠ No email - skipping email");
        }
        
        System.out.println("===================================================");
    }

    /**
     * Notify when appointment changes: SMS immediately, Email after 30s.
     */
    public void notifyAppointmentChange(Appointment appointment, LocalDateTime oldTime, LocalDateTime newTime) {
        System.out.println("========== NOTIFY APPOINTMENT CHANGE CALLED ==========");
        System.out.println("Appointment Code: " + appointment.getAppointmentCode());
        System.out.println("Old Time: " + oldTime + " -> New Time: " + newTime);
        System.out.println("Phone: " + appointment.getPhone());
        System.out.println("Email: " + appointment.getEmail());
        
        String magicLink = "http://localhost:5173/appointments/tracking?code=" + appointment.getAppointmentCode();

        // SMS Immediate - ALWAYS try to send if phone exists
        if (appointment.getPhone() != null && !appointment.getPhone().trim().isEmpty()) {
            try {
                String smsContent = "Lich kham thay doi! Ma: " + appointment.getAppointmentCode() + 
                                  ". Gio moi: " + newTime + ". Chi tiet: " + magicLink;
                smsService.sendSms(appointment.getPhone(), smsContent);
                System.out.println("✓ SMS sent successfully to: " + appointment.getPhone());
            } catch (Exception e) {
                System.err.println("✗ ERROR sending SMS to " + appointment.getPhone() + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠ No phone number - skipping SMS");
        }

        // Email Delayed
        if (appointment.getEmail() != null && !appointment.getEmail().trim().isEmpty()) {
            System.out.println("Scheduling delayed email in 30 seconds...");
            sendDelayedEmail(appointment, oldTime, newTime, magicLink);
        } else {
            System.out.println("⚠ No email - skipping email");
        }
        
        System.out.println("======================================================");
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

        // SMS Immediate - ALWAYS try to send if phone exists
        if (appointment.getPhone() != null && !appointment.getPhone().trim().isEmpty()) {
            try {
                String smsContent = "Trang thai lich kham thay doi! Ma: " + appointment.getAppointmentCode() + 
                                  ". Trang thai moi: " + newStatus + ". Chi tiet: " + magicLink;
                smsService.sendSms(appointment.getPhone(), smsContent);
                System.out.println("✓ SMS sent successfully to: " + appointment.getPhone());
            } catch (Exception e) {
                System.err.println("✗ ERROR sending SMS to " + appointment.getPhone() + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠ No phone number - skipping SMS");
        }

        // Email Delayed
        if (appointment.getEmail() != null && !appointment.getEmail().trim().isEmpty()) {
            System.out.println("Scheduling delayed email in 30 seconds...");
            sendDelayedStatusEmail(appointment, newStatus, magicLink);
        } else {
            System.out.println("⚠ No email - skipping email");
        }
        
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
            System.out.println("✓ Delayed email sent successfully to: " + appointment.getEmail());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Delayed email interrupted: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("✗ ERROR sending delayed email: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("✓ Delayed status email sent successfully to: " + appointment.getEmail());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Delayed email interrupted: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("✗ ERROR sending delayed status email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Send booking email notification only
     */
    public void sendBookingEmail(Appointment appointment, List<String> serviceNames) {
        System.out.println("========== SENDING BOOKING EMAIL ==========");
        System.out.println("Appointment Code: " + appointment.getAppointmentCode());
        System.out.println("Email: " + appointment.getEmail());
        
        if (appointment.getEmail() == null || appointment.getEmail().trim().isEmpty()) {
            System.out.println("⚠ Email is empty - SKIPPING");
            System.out.println("===========================================");
            return;
        }
        
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
            System.out.println("✓ Email sent successfully!");
        } catch (Exception e) {
            System.err.println("✗ ERROR sending email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send booking email", e);
        }
        System.out.println("===========================================");
    }
    
    /**
     * Send booking SMS notification only
     */
    public void sendBookingSms(Appointment appointment, List<String> serviceNames) {
        System.out.println("========== SENDING BOOKING SMS ==========");
        System.out.println("Appointment Code: " + appointment.getAppointmentCode());
        System.out.println("Phone: " + appointment.getPhone());
        
        if (appointment.getPhone() == null || appointment.getPhone().trim().isEmpty()) {
            System.out.println("⚠ Phone is empty - SKIPPING");
            System.out.println("=========================================");
            return;
        }
        
        String magicLink = "http://localhost:5173/appointments/tracking?code=" + appointment.getAppointmentCode();
        
        // Build service names string for SMS
        String servicesText = "";
        if (serviceNames != null && !serviceNames.isEmpty()) {
            servicesText = " Dich vu: " + String.join(", ", serviceNames) + ".";
        }
        
        String smsContent = "Dat lich thanh cong! Ma: " + appointment.getAppointmentCode() + 
                          "." + servicesText + " Xem chi tiet: " + magicLink;
        
        try {
            String result = smsService.sendSms(appointment.getPhone(), smsContent);
            System.out.println("✓ SMS sent successfully! SID: " + result);
        } catch (Exception e) {
            System.err.println("✗ ERROR sending SMS: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send booking SMS to " + appointment.getPhone(), e);
        }
        System.out.println("=========================================");
    }
}