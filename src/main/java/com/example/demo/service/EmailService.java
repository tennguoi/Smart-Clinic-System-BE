package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@EnableAsync   // KÍCH HOẠT ASYNC NGAY TRONG SERVICE
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendOtpEmail(String toEmail, String patientName, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Mã OTP - Tai-Mũi-Họng 2CTW");

        String emailContent = String.format(
                "Kính gửi %s,\n\n" +
                "Mã OTP của bạn là: %s\n\n" +
                "Vui lòng nhập mã này để tiếp tục. Mã OTP có hiệu lực trong vòng 5 phút.\n" +
                "Lưu ý: Không chia sẻ mã này với bất kỳ ai.\n\n" +
                "Trân trọng,\n" +
                "Tai-Mũi-Họng 2CTW",
                patientName, otp
        );
        message.setText(emailContent);
        mailSender.send(message);
    }

    @Async
    public void sendAppointmentConfirmation(String toEmail, String patientName, String appointmentCode,
                                            String appointmentTime, String confirmedByName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Xác nhận lịch khám - Tai-Mũi-Họng 2CTW");

        String emailContent = String.format(
                "Kính gửi %s,\n\n" +
                "Lịch khám của bạn đã được xác nhận!\n\n" +
                "- Họ tên: %s\n" +
                "- Mã lịch khám: %s\n" +
                "- Thời gian: %s\n" +
                "- Được xác nhận bởi: %s\n\n" +
                "Trân trọng,\n" +
                "Tai-Mũi-Họng 2CTW",
                patientName, patientName, appointmentCode, appointmentTime, confirmedByName
        );

        message.setText(emailContent);
        mailSender.send(message);
    }

    @Async
    public void sendAppointmentPendingNotification(String toEmail, String patientName, String appointmentCode,
                                                   LocalDateTime appointmentTime, String phone, String notes, String serviceNames) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Yêu cầu đặt lịch đã được tiếp nhận - Tai-Mũi-Họng 2CTW");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedTime = appointmentTime != null ? appointmentTime.format(formatter) : "Chưa xác định";
        String services = (serviceNames != null && !serviceNames.isEmpty()) ? serviceNames : "Không có dịch vụ nào";
        String userNotes = (notes != null && !notes.isEmpty()) ? notes : "Không có";

        String emailContent = String.format(
                "Kính gửi %s,\n\n" +
                "Chúng tôi đã nhận được yêu cầu đặt lịch khám của bạn.\n\n" +
                "- Họ tên: %s\n" +
                "- Mã lịch khám: %s\n" +
                "- Thời gian dự kiến: %s\n" +
                "- Số điện thoại: %s\n" +
                "- Dịch vụ: %s\n" +
                "- Ghi chú: %s\n\n" +
                "Trân trọng,\n" +
                "Tai-Mũi-Họng 2CTW",
                patientName, patientName, appointmentCode, formattedTime, phone, services, userNotes
        );

        message.setText(emailContent);
        mailSender.send(message);
    }

    @Async
    public void sendAppointmentReminder(String toEmail, String patientName, String appointmentCode, LocalDateTime appointmentTime) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Nhắc nhở lịch khám ngày mai - Tai-Mũi-Họng 2CTW");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedTime = appointmentTime.format(formatter);

        long hoursUntilAppointment = java.time.Duration.between(LocalDateTime.now(), appointmentTime).toHours();

        String emailContent = String.format(
                "Kính gửi %s,\n\n" +
                "Nhắc nhở lịch khám vào ngày mai.\n\n" +
                "- Họ tên: %s\n" +
                "- Mã lịch khám: %s\n" +
                "- Thời gian: %s\n" +
                "- Còn lại: %d giờ\n\n" +
                "Trân trọng,\n" +
                "Tai-Mũi-Họng 2CTW",
                patientName, patientName, appointmentCode, formattedTime, hoursUntilAppointment
        );

        message.setText(emailContent);
        mailSender.send(message);
    }

    @Async
    public void sendNewAppointmentNotification(String toEmail, String patientName, String appointmentCode,
                                               LocalDateTime appointmentTime, String notes, List<String> serviceNames, String magicLink) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Đặt lịch khám thành công - Tai-Mũi-Họng 2CTW");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedTime = appointmentTime.format(formatter);

        String services = (serviceNames != null && !serviceNames.isEmpty())
                ? String.join(", ", serviceNames)
                : "Không có dịch vụ nào";

        String note = (notes != null && !notes.isEmpty()) ? notes : "Không có";

        String emailContent = String.format(
                "Kính gửi %s,\n\n" +
                "Đặt lịch thành công:\n" +
                "- Mã lịch hẹn: %s\n" +
                "- Thời gian: %s\n" +
                "- Dịch vụ: %s\n" +
                "- Ghi chú: %s\n\n" +
                "%s\n\n" +
                "Trân trọng,\nTai-Mũi-Họng 2CTW",
                patientName, appointmentCode, formattedTime, services, note, magicLink
        );

        message.setText(emailContent);
        mailSender.send(message);
    }

    @Async
    public void sendAppointmentUpdateNotification(String toEmail, String patientName, String appointmentCode,
                                                  LocalDateTime oldTime, LocalDateTime newTime, String magicLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Thay đổi lịch khám - Tai-Mũi-Họng 2CTW");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        String emailContent = String.format(
                "Kính gửi %s,\n\n" +
                "Thời gian cũ: %s\n" +
                "Thời gian mới: %s\n\n%s",
                patientName,
                oldTime.format(formatter),
                newTime.format(formatter),
                magicLink
        );

        message.setText(emailContent);
        mailSender.send(message);
    }

    @Async
    public void sendAppointmentCancellationNotification(String toEmail, String patientName, String appointmentCode, String magicLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Hủy lịch khám - Tai-Mũi-Họng 2CTW");

        String emailContent = String.format(
                "Kính gửi %s,\n\nLịch khám %s đã bị hủy.\n\n%s",
                patientName, appointmentCode, magicLink
        );

        message.setText(emailContent);
        mailSender.send(message);
    }

    @Async
    public void sendAppointmentStatusChangeNotification(String toEmail, String patientName, String appointmentCode, String newStatus, String magicLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Cập nhật trạng thái lịch khám - Tai-Mũi-Họng 2CTW");

        String emailContent = String.format(
                "Kính gửi %s,\n\nTrạng thái mới: %s.\n\n%s",
                patientName, newStatus, magicLink
        );

        message.setText(emailContent);
        mailSender.send(message);
    }

}
