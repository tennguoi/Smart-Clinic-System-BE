package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Mã OTP - Smart Clinic System");
        message.setText("Mã OTP của bạn là: " + otp + "\nVui lòng nhập mã này để tiếp tục. Không chia sẻ với ai!");

        mailSender.send(message);
    }

    public void sendAppointmentConfirmation(String toEmail, String patientName, String appointmentCode, 
                                           String appointmentTime, String confirmedByName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Xác nhận lịch khám - Smart Clinic System");
        
        String emailContent = String.format(
            "Kính gửi %s,\n\n" +
            "Lịch khám của bạn đã được xác nhận!\n\n" +
            "Thông tin lịch khám:\n" +
            "- Mã lịch khám: %s\n" +
            "- Thời gian: %s\n" +
            "- Được xác nhận bởi: %s\n\n" +
            "Vui lòng đến đúng giờ. Nếu có thay đổi, vui lòng liên hệ phòng khám.\n\n" +
            "Trân trọng,\n" +
            "Smart Clinic System",
            patientName, appointmentCode, appointmentTime, confirmedByName
        );
        
        message.setText(emailContent);
        mailSender.send(message);
    }

    public void sendAppointmentPendingNotification(String toEmail, String patientName, String appointmentCode,
                                                   LocalDateTime appointmentTime) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Yêu cầu đặt lịch đã được tiếp nhận - Smart Clinic System");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedTime = appointmentTime != null ? appointmentTime.format(formatter) : "Chưa xác định";

        String emailContent = String.format(
                "Kính gửi %s,\n\n" +
                "Chúng tôi đã nhận được yêu cầu đặt lịch khám của bạn.\n\n" +
                "Thông tin lịch khám:\n" +
                "- Mã lịch khám: %s\n" +
                "- Thời gian dự kiến: %s\n\n" +
                "Nhân viên phòng khám sẽ sớm liên hệ với bạn để xác nhận lịch.\n" +
                "Nếu bạn cần thay đổi thông tin, vui lòng phản hồi email này.\n\n" +
                "Trân trọng,\n" +
                "Smart Clinic System",
                patientName, appointmentCode, formattedTime
        );

        message.setText(emailContent);
        mailSender.send(message);
    }

    public void sendAppointmentReminder(String toEmail, String patientName, String appointmentCode, LocalDateTime appointmentTime) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Nhắc nhở lịch khám - Smart Clinic System");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedTime = appointmentTime != null ? appointmentTime.format(formatter) : "Chưa xác định";

        String emailContent = String.format(
            "Kính gửi %s,\n\n" +
            "Đây là email nhắc nhở bạn có lịch khám vào ngày mai.\n\n" +
            "Thông tin lịch khám:\n" +
            "- Mã lịch khám: %s\n" +
            "- Thời gian: %s\n\n" +
            "Vui lòng đến đúng giờ để được phục vụ tốt nhất.\n" +
            "Nếu bạn cần thay đổi hoặc hủy lịch, vui lòng liên hệ với chúng tôi sớm nhất có thể.\n\n" +
            "Trân trọng,\n" +
            "Smart Clinic System",
            patientName, appointmentCode, formattedTime
        );

        message.setText(emailContent);
        mailSender.send(message);
    }
}