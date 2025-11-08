package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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
}