package com.example.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EnableAsync
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8082}")
    private String baseUrl;

    private String getLogoUrl() {
        return baseUrl + "/images/logo.png";
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = HTML

        mailSender.send(message);
    }

    @Async
    public void sendOtpEmail(String toEmail, String patientName, String otp) {
        try {
            var template = emailTemplateService.getTemplateByType("OTP");

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("patientName", patientName);
            placeholders.put("otp", otp);
            placeholders.put("logoUrl", getLogoUrl());

            String subject = emailTemplateService.replacePlaceholders(template.getSubject(), placeholders);
            String content = emailTemplateService.replacePlaceholders(template.getContent(), placeholders);

            sendHtmlEmail(toEmail, subject, content);
        } catch (Exception e) {
            System.err.println("❌ OTP EMAIL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void sendAppointmentConfirmation(String toEmail, String patientName, String appointmentCode,
            String appointmentTime, String confirmedByName) {
        try {
            var template = emailTemplateService.getTemplateByType("APPOINTMENT_CONFIRMATION");

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("patientName", patientName);
            placeholders.put("appointmentCode", appointmentCode);
            placeholders.put("appointmentTime", appointmentTime);
            placeholders.put("confirmedByName", confirmedByName);
            placeholders.put("logoUrl", getLogoUrl());

            String subject = emailTemplateService.replacePlaceholders(template.getSubject(), placeholders);
            String content = emailTemplateService.replacePlaceholders(template.getContent(), placeholders);

            sendHtmlEmail(toEmail, subject, content);
        } catch (Exception e) {
            System.err.println("❌ APPOINTMENT CONFIRMATION EMAIL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void sendAppointmentPendingNotification(String toEmail, String patientName, String appointmentCode,
            LocalDateTime appointmentTime, String phone, String notes, String serviceNames) {
        try {
            var template = emailTemplateService.getTemplateByType("APPOINTMENT_PENDING");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String formattedTime = appointmentTime != null ? appointmentTime.format(formatter) : "Chưa xác định";
            String services = (serviceNames != null && !serviceNames.isEmpty()) ? serviceNames : "Không có dịch vụ nào";
            String userNotes = (notes != null && !notes.isEmpty()) ? notes : "Không có";

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("patientName", patientName);
            placeholders.put("appointmentCode", appointmentCode);
            placeholders.put("appointmentTime", formattedTime);
            placeholders.put("phone", phone);
            placeholders.put("serviceNames", services);
            placeholders.put("notes", userNotes);
            placeholders.put("logoUrl", getLogoUrl());

            String subject = emailTemplateService.replacePlaceholders(template.getSubject(), placeholders);
            String content = emailTemplateService.replacePlaceholders(template.getContent(), placeholders);

            sendHtmlEmail(toEmail, subject, content);
        } catch (Exception e) {
            System.err.println("❌ APPOINTMENT PENDING EMAIL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void sendAppointmentReminder(String toEmail, String patientName, String appointmentCode,
            LocalDateTime appointmentTime) {
        try {
            var template = emailTemplateService.getTemplateByType("APPOINTMENT_REMINDER");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String formattedTime = appointmentTime != null ? appointmentTime.format(formatter) : "Chưa xác định";

            LocalDateTime now = LocalDateTime.now();
            long hoursUntilAppointment = java.time.Duration.between(now, appointmentTime).toHours();

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("patientName", patientName);
            placeholders.put("appointmentCode", appointmentCode);
            placeholders.put("appointmentTime", formattedTime);
            placeholders.put("hoursUntilAppointment", String.valueOf(hoursUntilAppointment));
            placeholders.put("clinicEmail", fromEmail);
            placeholders.put("logoUrl", getLogoUrl());

            String subject = emailTemplateService.replacePlaceholders(template.getSubject(), placeholders);
            String content = emailTemplateService.replacePlaceholders(template.getContent(), placeholders);

            sendHtmlEmail(toEmail, subject, content);
        } catch (Exception e) {
            System.err.println("❌ APPOINTMENT REMINDER EMAIL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void sendNewAppointmentNotification(String toEmail, String patientName, String appointmentCode,
            LocalDateTime appointmentTime, String notes, List<String> serviceNames, String magicLink) {
        try {
            System.out.println("========== SENDING HTML EMAIL VIA SMTP ==========");
            System.out.println("To: " + toEmail);

            var template = emailTemplateService.getTemplateByType("NEW_APPOINTMENT");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String formattedTime = appointmentTime != null ? appointmentTime.format(formatter) : "Chưa xác định";
            String services = (serviceNames != null && !serviceNames.isEmpty()) ? String.join(", ", serviceNames)
                    : "Không có dịch vụ nào";
            String note = (notes != null && !notes.trim().isEmpty()) ? notes : "Không có";

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("patientName", patientName);
            placeholders.put("appointmentCode", appointmentCode);
            placeholders.put("appointmentTime", formattedTime);
            placeholders.put("serviceNames", services);
            placeholders.put("notes", note);
            placeholders.put("magicLink", magicLink);
            placeholders.put("logoUrl", getLogoUrl());

            String subject = emailTemplateService.replacePlaceholders(template.getSubject(), placeholders);
            String content = emailTemplateService.replacePlaceholders(template.getContent(), placeholders);

            System.out.println("About to send HTML email via mailSender...");
            sendHtmlEmail(toEmail, subject, content);
            System.out.println("✓ HTML Email sent successfully to: " + toEmail);
            System.out.println("================================================");

        } catch (Exception e) {
            System.err.println("❌ NEW APPOINTMENT EMAIL ERROR: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Async
    public void sendAppointmentUpdateNotification(String toEmail, String patientName, String appointmentCode,
            LocalDateTime oldTime, LocalDateTime newTime, String magicLink) {
        try {
            var template = emailTemplateService.getTemplateByType("APPOINTMENT_UPDATE");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String formattedOldTime = oldTime != null ? oldTime.format(formatter) : "Chưa xác định";
            String formattedNewTime = newTime != null ? newTime.format(formatter) : "Chưa xác định";

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("patientName", patientName);
            placeholders.put("appointmentCode", appointmentCode);
            placeholders.put("oldTime", formattedOldTime);
            placeholders.put("newTime", formattedNewTime);
            placeholders.put("magicLink", magicLink);
            placeholders.put("logoUrl", getLogoUrl());

            String subject = emailTemplateService.replacePlaceholders(template.getSubject(), placeholders);
            String content = emailTemplateService.replacePlaceholders(template.getContent(), placeholders);

            sendHtmlEmail(toEmail, subject, content);
        } catch (Exception e) {
            System.err.println("❌ APPOINTMENT UPDATE EMAIL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void sendAppointmentCancellationNotification(String toEmail, String patientName, String appointmentCode,
            String magicLink, String cancelReason) {
        try {
            var template = emailTemplateService.getTemplateByType("APPOINTMENT_CANCELLATION");

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("patientName", patientName);
            placeholders.put("appointmentCode", appointmentCode);
            placeholders.put("magicLink", magicLink);
            placeholders.put("logoUrl", getLogoUrl());
            placeholders.put("cancelReason", cancelReason != null && !cancelReason.trim().isEmpty() 
                ? cancelReason 
                : "Không có lý do cụ thể được cung cấp");

            String subject = emailTemplateService.replacePlaceholders(template.getSubject(), placeholders);
            String content = emailTemplateService.replacePlaceholders(template.getContent(), placeholders);

            sendHtmlEmail(toEmail, subject, content);
        } catch (Exception e) {
            System.err.println("❌ APPOINTMENT CANCELLATION EMAIL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void sendAppointmentStatusChangeNotification(String toEmail, String patientName, String appointmentCode,
            String newStatus, String magicLink) {
        try {
            var template = emailTemplateService.getTemplateByType("APPOINTMENT_STATUS_CHANGE");

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("patientName", patientName);
            placeholders.put("appointmentCode", appointmentCode);
            placeholders.put("newStatus", newStatus);
            placeholders.put("magicLink", magicLink);
            placeholders.put("logoUrl", getLogoUrl());

            String subject = emailTemplateService.replacePlaceholders(template.getSubject(), placeholders);
            String content = emailTemplateService.replacePlaceholders(template.getContent(), placeholders);

            sendHtmlEmail(toEmail, subject, content);
        } catch (Exception e) {
            System.err.println("❌ APPOINTMENT STATUS CHANGE EMAIL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}