package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service để gửi email
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String patientName, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Mã OTP - Smart Clinic System");
        
        String emailContent = String.format(
            "Kính gửi %s,\n\n" +
            "Mã OTP của bạn là: %s\n\n" +
            "Vui lòng nhập mã này để tiếp tục. Mã OTP có hiệu lực trong vòng 5 phút.\n" +
            "Lưu ý: Không chia sẻ mã này với bất kỳ ai.\n\n" +
            "Trân trọng,\n" +
            "Smart Clinic System",
            patientName, otp
        );
        
        message.setText(emailContent);
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
            "- Họ tên: %s\n" +
            "- Mã lịch khám: %s\n" +
            "- Thời gian: %s\n" +
            "- Được xác nhận bởi: %s\n\n" +
            "Vui lòng đến đúng giờ. Nếu có thay đổi, vui lòng liên hệ phòng khám.\n\n" +
            "Trân trọng,\n" +
            "Smart Clinic System",
            patientName, patientName, appointmentCode, appointmentTime, confirmedByName
        );
        
        message.setText(emailContent);
        mailSender.send(message);
    }

    public void sendAppointmentPendingNotification(String toEmail, String patientName, String appointmentCode,
                                                   LocalDateTime appointmentTime, String phone, String notes, String serviceNames) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Yêu cầu đặt lịch đã được tiếp nhận - Smart Clinic System");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedTime = appointmentTime != null ? appointmentTime.format(formatter) : "Chưa xác định";
        String services = (serviceNames != null && !serviceNames.isEmpty()) ? serviceNames : "Không có dịch vụ nào";
        String userNotes = (notes != null && !notes.isEmpty()) ? notes : "Không có";

        String emailContent = String.format(
                "Kính gửi %s,\n\n" +
                "Chúng tôi đã nhận được yêu cầu đặt lịch khám của bạn.\n\n" +
                "Thông tin chi tiết:\n" +
                "- Họ tên: %s\n" +
                "- Mã lịch khám: %s\n" +
                "- Thời gian dự kiến: %s\n" +
                "- Số điện thoại: %s\n" +
                "- Dịch vụ đăng ký: %s\n" +
                "- Triệu chứng/Ghi chú: %s\n\n" +
                "Nhân viên phòng khám sẽ sớm liên hệ với bạn để xác nhận lịch.\n" +
                "Nếu bạn cần thay đổi thông tin, vui lòng phản hồi email này.\n\n" +
                "Trân trọng,\n" +
                "Smart Clinic System",
                patientName, patientName, appointmentCode, formattedTime, phone, services, userNotes
        );

        message.setText(emailContent);
        mailSender.send(message);
    }

    public void sendAppointmentReminder(String toEmail, String patientName, String appointmentCode, LocalDateTime appointmentTime) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromEmail);
    message.setTo(toEmail);
    message.setSubject(" Nhắc nhở lịch khám ngày mai - Smart Clinic System");

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    String formattedTime = appointmentTime != null ? appointmentTime.format(formatter) : "Chưa xác định";
    
    // Thêm thông tin về thời gian còn lại
    LocalDateTime now = LocalDateTime.now();
    long hoursUntilAppointment = java.time.Duration.between(now, appointmentTime).toHours();

    String emailContent = String.format(
        "Kính gửi %s,\n\n" +
        "Đây là email nhắc nhở lịch khám của bạn vào NGÀY MAI.\n\n" +
        "Thông tin lịch khám:\n" +
        "- Họ tên: %s\n" +
        "- Mã lịch khám: %s\n" +
        "- Thời gian: %s\n" +
        "- Còn lại: Khoảng %d giờ nữa\n\n" +
        " Lưu ý quan trọng:\n" +
        "- Vui lòng đến sớm 15 phút trước giờ hẹn\n" +
        "- Mang theo giấy tờ tùy thân (CMND/CCCD)\n" +
        "- Nếu không thể đến, vui lòng liên hệ với chúng tôi sớm nhất có thể\n\n" +
        "Nếu bạn cần thay đổi hoặc hủy lịch, vui lòng liên hệ với chúng tôi qua:\n" +
        "- Hotline: [Số điện thoại phòng khám]\n" +
        "- Email: %s\n\n" +
        "Trân trọng,\n" +
        "Smart Clinic System",
        patientName, patientName, appointmentCode, formattedTime, hoursUntilAppointment, fromEmail
    );

    message.setText(emailContent);
    mailSender.send(message);
}
    /**
     * Gửi email thông báo khi tạo lịch hẹn mới
     */
    public void sendNewAppointmentNotification(String toEmail, String patientName, String appointmentCode, 
                                             LocalDateTime appointmentTime, String notes, List<String> serviceNames) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Đặt lịch khám thành công - Smart Clinic System");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedTime = appointmentTime != null ? appointmentTime.format(formatter) : "Chưa xác định";
        String services = (serviceNames != null && !serviceNames.isEmpty()) ? 
                         String.join(", ", serviceNames) : "Không có dịch vụ nào";
        String note = (notes != null && !notes.trim().isEmpty()) ? notes : "Không có";

        String emailContent = String.format(
            "Kính gửi %s,\n\n" +
            "Cảm ơn bạn đã đặt lịch khám tại phòng khám của chúng tôi.\n\n" +
            "Thông tin lịch hẹn:\n" +
            "- Mã lịch hẹn: %s\n" +
            "- Họ tên: %s\n" +
            "- Thời gian: %s\n" +
            "- Dịch vụ đăng ký: %s\n" +
            "- Ghi chú: %s\n\n" +
            "Vui lòng đến đúng giờ để được phục vụ tốt nhất.\n" +
            "Nếu bạn cần thay đổi hoặc hủy lịch, vui lòng liên hệ với chúng tôi sớm nhất có thể.\n\n" +
            "Trân trọng,\n" +
            "Smart Clinic System",
            patientName, appointmentCode, patientName, formattedTime, services, note
        );

        message.setText(emailContent);
        mailSender.send(message);
    }
}