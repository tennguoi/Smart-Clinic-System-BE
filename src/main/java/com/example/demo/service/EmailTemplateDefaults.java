package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Default HTML email templates with placeholders
 */
public class EmailTemplateDefaults {

    public static Map<String, String> getDefault(String templateType) {
        Map<String, String> template = new HashMap<>();

        switch (templateType) {
            case "OTP":
                template.put("subject", "Mã OTP - Tai-Mũi-Họng 2CTW");
                template.put("content", getOtpTemplate());
                break;
            case "APPOINTMENT_CONFIRMATION":
                template.put("subject", "Xác nhận lịch khám - Tai-Mũi-Họng 2CTW");
                template.put("content", getAppointmentConfirmationTemplate());
                break;
            case "APPOINTMENT_PENDING":
                template.put("subject", "Yêu cầu đặt lịch đã được tiếp nhận - Tai-Mũi-Họng 2CTW");
                template.put("content", getAppointmentPendingTemplate());
                break;
            case "APPOINTMENT_REMINDER":
                template.put("subject", "Nhắc nhở lịch khám ngày mai - Tai-Mũi-Họng 2CTW");
                template.put("content", getAppointmentReminderTemplate());
                break;
            case "NEW_APPOINTMENT":
                template.put("subject", "Đặt lịch khám thành công - Tai-Mũi-Họng 2CTW");
                template.put("content", getNewAppointmentTemplate());
                break;
            case "APPOINTMENT_UPDATE":
                template.put("subject", "Thông báo thay đổi lịch khám - Tai-Mũi-Họng 2CTW");
                template.put("content", getAppointmentUpdateTemplate());
                break;
            case "APPOINTMENT_CANCELLATION":
                template.put("subject", "Thông báo hủy lịch khám - Tai-Mũi-Họng 2CTW");
                template.put("content", getAppointmentCancellationTemplate());
                break;
            case "APPOINTMENT_STATUS_CHANGE":
                template.put("subject", "Cập nhật trạng thái lịch khám - Tai-Mũi-Họng 2CTW");
                template.put("content", getAppointmentStatusChangeTemplate());
                break;
            default:
                template.put("subject", "Thông báo - Tai-Mũi-Họng 2CTW");
                template.put("content", getDefaultTemplate());
        }

        return template;
    }

    private static String getEmailHeader() {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset=\"UTF-8\"><style>" +
                "body{font-family:Arial,sans-serif;line-height:1.6;color:#333;max-width:600px;margin:0 auto;padding:20px;}"
                +
                ".header{text-align:center;padding:20px;background:#0056b3;color:white;border-radius:8px 8px 0 0;}" +
                ".logo{max-width:150px;height:auto;margin-bottom:10px;}" +
                ".content{background:#f9f9f9;padding:30px;border-radius:0 0 8px 8px;}" +
                ".info-box{background:white;padding:15px;margin:15px 0;border-left:4px solid #0056b3;border-radius:4px;}"
                +
                ".button{display:inline-block;padding:12px 30px;background:#0056b3;color:white;text-decoration:none;border-radius:5px;margin:15px 0;}"
                +
                ".footer{text-align:center;padding:20px;color:#666;font-size:12px;}" +
                "</style></head><body>";
    }

    private static String getEmailFooter() {
        return "<div class=\"footer\">" +
                "<p>Trân trọng,<br><strong>Phòng khám Tai-Mũi-Họng 2CTW</strong></p>" +
                "<p style=\"font-size:11px;color:#999;\">Email này được gửi tự động, vui lòng không trả lời.</p>" +
                "</div></body></html>";
    }

    private static String getOtpTemplate() {
        return getEmailHeader() +
                "<div class=\"header\">" +
                "<img src=\"{{logoUrl}}\" alt=\"Logo\" class=\"logo\">" +
                "<h2>Mã Xác Thực OTP</h2>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<div class=\"info-box\">" +
                "<h3 style=\"color:#0056b3;margin:0;\">Mã OTP của bạn: <span style=\"font-size:24px;letter-spacing:3px;\">{{otp}}</span></h3>"
                +
                "</div>" +
                "<p>Vui lòng nhập mã này để tiếp tục. Mã OTP có hiệu lực trong vòng <strong>5 phút</strong>.</p>" +
                "<p style=\"color:#d9534f;\"><strong>⚠️ Lưu ý:</strong> Không chia sẻ mã này với bất kỳ ai.</p>" +
                "</div>" +
                getEmailFooter();
    }

    private static String getAppointmentConfirmationTemplate() {
        return getEmailHeader() +
                "<div class=\"header\">" +
                "<img src=\"{{logoUrl}}\" alt=\"Logo\" class=\"logo\">" +
                "<h2>✅ Xác Nhận Lịch Khám</h2>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p>Lịch khám của bạn đã được <strong style=\"color:#28a745;\">XÁC NHẬN</strong>!</p>" +
                "<div class=\"info-box\">" +
                "<p><strong>📋 Mã lịch khám:</strong> {{appointmentCode}}</p>" +
                "<p><strong>👤 Họ tên:</strong> {{patientName}}</p>" +
                "<p><strong>🕐 Thời gian:</strong> {{appointmentTime}}</p>" +
                "<p><strong>👨‍⚕️ Được xác nhận bởi:</strong> {{confirmedByName}}</p>" +
                "</div>" +
                "<p>Vui lòng đến <strong>đúng giờ</strong>. Nếu có thay đổi, vui lòng liên hệ phòng khám.</p>" +
                "</div>" +
                getEmailFooter();
    }

    private static String getAppointmentPendingTemplate() {
        return getEmailHeader() +
                "<div class=\"header\">" +
                "<img src=\"{{logoUrl}}\" alt=\"Logo\" class=\"logo\">" +
                "<h2>📨 Yêu Cầu Đặt Lịch Đã Nhận</h2>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p>Chúng tôi đã nhận được yêu cầu đặt lịch khám của bạn.</p>" +
                "<div class=\"info-box\">" +
                "<p><strong>📋 Mã lịch khám:</strong> {{appointmentCode}}</p>" +
                "<p><strong>👤 Họ tên:</strong> {{patientName}}</p>" +
                "<p><strong>🕐 Thời gian dự kiến:</strong> {{appointmentTime}}</p>" +
                "<p><strong>📞 Số điện thoại:</strong> {{phone}}</p>" +
                "<p><strong>🏥 Dịch vụ đăng ký:</strong> {{serviceNames}}</p>" +
                "<p><strong>📝 Triệu chứng/Ghi chú:</strong> {{notes}}</p>" +
                "</div>" +
                "<p>Nhân viên phòng khám sẽ sớm liên hệ với bạn để xác nhận lịch.</p>" +
                "</div>" +
                getEmailFooter();
    }

    private static String getAppointmentReminderTemplate() {
        return getEmailHeader() +
                "<div class=\"header\">" +
                "<img src=\"{{logoUrl}}\" alt=\"Logo\" class=\"logo\">" +
                "<h2>🔔 Nhắc Nhở Lịch Khám</h2>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p>Đây là email nhắc nhở lịch khám của bạn vào <strong>NGÀY MAI</strong>.</p>" +
                "<div class=\"info-box\">" +
                "<p><strong>📋 Mã lịch khám:</strong> {{appointmentCode}}</p>" +
                "<p><strong>👤 Họ tên:</strong> {{patientName}}</p>" +
                "<p><strong>🕐 Thời gian:</strong> {{appointmentTime}}</p>" +
                "<p><strong>⏰ Còn lại:</strong> Khoảng {{hoursUntilAppointment}} giờ nữa</p>" +
                "</div>" +
                "<div class=\"info-box\" style=\"border-left-color:#ffc107;\">" +
                "<p><strong>📌 Lưu ý quan trọng:</strong></p>" +
                "<ul>" +
                "<li>Đến sớm 15 phút trước giờ hẹn</li>" +
                "<li>Mang theo giấy tờ tùy thân (CMND/CCCD)</li>" +
                "<li>Nếu không thể đến, vui lòng liên hệ sớm nhất</li>" +
                "</ul>" +
                "</div>" +
                "<p>📧 Email: {{clinicEmail}}</p>" +
                "</div>" +
                getEmailFooter();
    }

    private static String getNewAppointmentTemplate() {
        return getEmailHeader() +
                "<div class=\"header\">" +
                "<img src=\"{{logoUrl}}\" alt=\"Logo\" class=\"logo\">" +
                "<h2>✅ Đặt Lịch Thành Công</h2>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p>Cảm ơn bạn đã đặt lịch khám tại phòng khám của chúng tôi.</p>" +
                "<div class=\"info-box\">" +
                "<p><strong>📋 Mã lịch hẹn:</strong> {{appointmentCode}}</p>" +
                "<p><strong>👤 Họ tên:</strong> {{patientName}}</p>" +
                "<p><strong>🕐 Thời gian:</strong> {{appointmentTime}}</p>" +
                "<p><strong>🏥 Dịch vụ đăng ký:</strong> {{serviceNames}}</p>" +
                "<p><strong>📝 Ghi chú:</strong> {{notes}}</p>" +
                "</div>" +
                "<a href=\"{{magicLink}}\" class=\"button\">Theo Dõi Lịch Khám</a>" +
                "<p>Vui lòng đến đúng giờ để được phục vụ tốt nhất.</p>" +
                "</div>" +
                getEmailFooter();
    }

    private static String getAppointmentUpdateTemplate() {
        return getEmailHeader() +
                "<div class=\"header\">" +
                "<img src=\"{{logoUrl}}\" alt=\"Logo\" class=\"logo\">" +
                "<h2>🔄 Thay Đổi Lịch Khám</h2>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p>Lịch khám của bạn đã được <strong style=\"color:#ffc107;\">THAY ĐỔI</strong>.</p>" +
                "<div class=\"info-box\">" +
                "<p><strong>📋 Mã lịch khám:</strong> {{appointmentCode}}</p>" +
                "<p><strong>⏰ Thời gian cũ:</strong> <del>{{oldTime}}</del></p>" +
                "<p><strong style=\"color:#0056b3;\">🕐 Thời gian MỚI:</strong> {{newTime}}</p>" +
                "</div>" +
                "<a href=\"{{magicLink}}\" class=\"button\">Xem Chi Tiết</a>" +
                "<p>Nếu bạn có thắc mắc về sự thay đổi này, vui lòng liên hệ với phòng khám.</p>" +
                "</div>" +
                getEmailFooter();
    }

    private static String getAppointmentCancellationTemplate() {
        return getEmailHeader() +
                "<div class=\"header\" style=\"background:#d9534f;\">" +
                "<img src=\"{{logoUrl}}\" alt=\"Logo\" class=\"logo\">" +
                "<h2>❌ Hủy Lịch Khám</h2>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p>Lịch khám của bạn với mã <strong>{{appointmentCode}}</strong> đã bị <strong style=\"color:#d9534f;\">HỦY</strong>.</p>" +
                "<div class=\"info-box\" style=\"border-left-color:#d9534f;\">" +
                "<p><strong>📝 Lý do hủy:</strong></p>" +
                "<p>{{cancelReason}}</p>" +
                "</div>" +
                "<p>Nếu đây là sự nhầm lẫn hoặc bạn muốn đặt lại lịch, vui lòng liên hệ với chúng tôi hoặc:</p>" +
                "<a href=\"{{magicLink}}\" class=\"button\" style=\"background:#d9534f;\">Đặt Lại Lịch</a>" +
                "</div>" +
                getEmailFooter();
    }

    private static String getAppointmentStatusChangeTemplate() {
        return getEmailHeader() +
                "<div class=\"header\">" +
                "<img src=\"{{logoUrl}}\" alt=\"Logo\" class=\"logo\">" +
                "<h2>📢 Cập Nhật Trạng Thái</h2>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p>Trạng thái lịch khám (Mã: <strong>{{appointmentCode}}</strong>) của bạn đã được cập nhật thành:</p>"
                +
                "<div class=\"info-box\">" +
                "<h3 style=\"color:#0056b3;margin:0;\">{{newStatus}}</h3>" +
                "</div>" +
                "<a href=\"{{magicLink}}\" class=\"button\">Theo Dõi Chi Tiết</a>" +
                "</div>" +
                getEmailFooter();
    }

    private static String getDefaultTemplate() {
        return getEmailHeader() +
                "<div class=\"header\">" +
                "<img src=\"{{logoUrl}}\" alt=\"Logo\" class=\"logo\">" +
                "<h2>Thông Báo</h2>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p>{{content}}</p>" +
                "</div>" +
                getEmailFooter();
    }
}
