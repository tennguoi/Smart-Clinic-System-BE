package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Default HTML email templates with placeholders.
 * Templates are split into: header (fixed), bodyContent (editable), footer
 * (fixed)
 */
public class EmailTemplateDefaults {

    public static Map<String, String> getDefault(String templateType) {
        Map<String, String> template = new HashMap<>();

        switch (templateType) {
            case "OTP":
                template.put("subject", "Mã OTP - Tai-Mũi-Họng 2CTW");
                template.put("bodyContent", getOtpBodyContent());
                template.put("content", buildFullContent("Mã Xác Thực OTP", getOtpBodyContent()));
                break;
            case "APPOINTMENT_CONFIRMATION":
                template.put("subject", "Xác nhận lịch khám - Tai-Mũi-Họng 2CTW");
                template.put("bodyContent", getAppointmentConfirmationBodyContent());
                template.put("content",
                        buildFullContent("Xác Nhận Lịch Khám", getAppointmentConfirmationBodyContent()));
                break;
            case "APPOINTMENT_PENDING":
                template.put("subject", "Yêu cầu đặt lịch đã được tiếp nhận - Tai-Mũi-Họng 2CTW");
                template.put("bodyContent", getAppointmentPendingBodyContent());
                template.put("content",
                        buildFullContent("Yêu Cầu Đặt Lịch Đã Nhận", getAppointmentPendingBodyContent()));
                break;
            case "APPOINTMENT_REMINDER":
                template.put("subject", "Nhắc nhở lịch khám ngày mai - Tai-Mũi-Họng 2CTW");
                template.put("bodyContent", getAppointmentReminderBodyContent());
                template.put("content", buildFullContent("Nhắc Nhở Lịch Khám", getAppointmentReminderBodyContent()));
                break;
            case "NEW_APPOINTMENT":
                template.put("subject", "Đặt lịch khám thành công - Tai-Mũi-Họng 2CTW");
                template.put("bodyContent", getNewAppointmentBodyContent());
                template.put("content", buildFullContent("Đặt Lịch Thành Công", getNewAppointmentBodyContent()));
                break;
            case "APPOINTMENT_UPDATE":
                template.put("subject", "Thông báo thay đổi lịch khám - Tai-Mũi-Họng 2CTW");
                template.put("bodyContent", getAppointmentUpdateBodyContent());
                template.put("content", buildFullContent("Thay Đổi Lịch Khám", getAppointmentUpdateBodyContent()));
                break;
            case "APPOINTMENT_CANCELLATION":
                template.put("subject", "Thông báo hủy lịch khám - Tai-Mũi-Họng 2CTW");
                template.put("bodyContent", getAppointmentCancellationBodyContent());
                template.put("content",
                        buildFullContentWithRedHeader("Hủy Lịch Khám", getAppointmentCancellationBodyContent()));
                break;
            case "APPOINTMENT_STATUS_CHANGE":
                template.put("subject", "Cập nhật trạng thái lịch khám - Tai-Mũi-Họng 2CTW");
                template.put("bodyContent", getAppointmentStatusChangeBodyContent());
                template.put("content",
                        buildFullContent("Cập Nhật Trạng Thái", getAppointmentStatusChangeBodyContent()));
                break;
            default:
                template.put("subject", "Thông báo - Tai-Mũi-Họng 2CTW");
                template.put("bodyContent", getDefaultBodyContent());
                template.put("content", buildFullContent("Thông Báo", getDefaultBodyContent()));
        }

        return template;
    }

    /**
     * Build full email content from header + title + bodyContent + footer
     */
    public static String buildFullContent(String title, String bodyContent) {
        return getEmailHeader() +
                "<div class=\"header\">" +
                "<img src=\"{{logoUrl}}\" alt=\"Logo\" class=\"logo\">" +
                "<h2>" + title + "</h2>" +
                "</div>" +
                "<div class=\"content\">" +
                bodyContent +
                "</div>" +
                getEmailFooter();
    }

    /**
     * Build full email content with red header (for cancellation emails)
     */
    public static String buildFullContentWithRedHeader(String title, String bodyContent) {
        return getEmailHeader() +
                "<div class=\"header\" style=\"background:#d9534f;\">" +
                "<img src=\"{{logoUrl}}\" alt=\"Logo\" class=\"logo\">" +
                "<h2>" + title + "</h2>" +
                "</div>" +
                "<div class=\"content\">" +
                bodyContent +
                "</div>" +
                getEmailFooter();
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

    // ==================== BODY CONTENT ONLY (for React Quill editing)
    // ====================

    private static String getOtpBodyContent() {
        return "<p>Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<div class=\"info-box\">" +
                "<h3 style=\"color:#0056b3;margin:0;\">Mã OTP của bạn: <span style=\"font-size:24px;letter-spacing:3px;\">{{otp}}</span></h3>"
                +
                "</div>" +
                "<p>Vui lòng nhập mã này để tiếp tục. Mã OTP có hiệu lực trong vòng <strong>5 phút</strong>.</p>" +
                "<p style=\"color:#d9534f;\"><strong>⚠️ Lưu ý:</strong> Không chia sẻ mã này với bất kỳ ai.</p>";
    }

    private static String getAppointmentConfirmationBodyContent() {
        return "<p>Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p>Lịch khám của bạn đã được <strong style=\"color:#28a745;\">XÁC NHẬN</strong>!</p>" +
                "<div class=\"info-box\">" +
                "<p><strong>Mã lịch khám:</strong> {{appointmentCode}}</p>" +
                "<p><strong>Họ tên:</strong> {{patientName}}</p>" +
                "<p><strong>Thời gian:</strong> {{appointmentTime}}</p>" +
                "<p><strong>Được xác nhận bởi:</strong> {{confirmedByName}}</p>" +
                "</div>" +
                "<p>Vui lòng đến <strong>đúng giờ</strong>. Nếu có thay đổi, vui lòng liên hệ phòng khám.</p>";
    }

    private static String getAppointmentPendingBodyContent() {
        return "<p>Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p>Chúng tôi đã nhận được yêu cầu đặt lịch khám của bạn.</p>" +
                "<div class=\"info-box\">" +
                "<p><strong>Mã lịch khám:</strong> {{appointmentCode}}</p>" +
                "<p><strong>Họ tên:</strong> {{patientName}}</p>" +
                "<p><strong>Thời gian dự kiến:</strong> {{appointmentTime}}</p>" +
                "<p><strong>Số điện thoại:</strong> {{phone}}</p>" +
                "<p><strong>Dịch vụ đăng ký:</strong> {{serviceNames}}</p>" +
                "<p><strong>Triệu chứng/Ghi chú:</strong> {{notes}}</p>" +
                "</div>" +
                "<p>Nhân viên phòng khám sẽ sớm liên hệ với bạn để xác nhận lịch.</p>";
    }

    private static String getAppointmentReminderBodyContent() {
        return "<p>Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p>Đây là email nhắc nhở lịch khám của bạn vào <strong>NGÀY MAI</strong>.</p>" +
                "<div class=\"info-box\">" +
                "<p><strong>Mã lịch khám:</strong> {{appointmentCode}}</p>" +
                "<p><strong>Họ tên:</strong> {{patientName}}</p>" +
                "<p><strong>Thời gian:</strong> {{appointmentTime}}</p>" +
                "<p><strong>Thời gian còn lại:</strong> Khoảng {{hoursUntilAppointment}} giờ nữa</p>" +
                "</div>" +
                "<div class=\"info-box\" style=\"border-left-color:#ffc107;\">" +
                "<p><strong>Lưu ý quan trọng:</strong></p>" +
                "<ul>" +
                "<li>Đến sớm 15 phút trước giờ hẹn</li>" +
                "<li>Mang theo giấy tờ tùy thân (CMND/CCCD)</li>" +
                "<li>Nếu không thể đến, vui lòng liên hệ sớm nhất</li>" +
                "</ul>" +
                "</div>" +
                "<p>Email: {{clinicEmail}}</p>";
    }

    private static String getNewAppointmentBodyContent() {
        return "<p>Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p>Cảm ơn bạn đã đặt lịch khám tại phòng khám của chúng tôi.</p>" +
                "<div class=\"info-box\">" +
                "<p><strong>Mã lịch hẹn:</strong> {{appointmentCode}}</p>" +
                "<p><strong>Họ tên:</strong> {{patientName}}</p>" +
                "<p><strong>Thời gian:</strong> {{appointmentTime}}</p>" +
                "<p><strong>Dịch vụ đăng ký:</strong> {{serviceNames}}</p>" +
                "<p><strong>Ghi chú:</strong> {{notes}}</p>" +
                "</div>" +
                "<a href=\"{{magicLink}}\" class=\"button\">Theo Dõi Lịch Khám</a>" +
                "<p>Vui lòng đến đúng giờ để được phục vụ tốt nhất.</p>";
    }

    private static String getAppointmentUpdateBodyContent() {
        return "<p>Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p>Lịch khám của bạn đã được <strong style=\"color:#ffc107;\">THAY ĐỔI</strong>.</p>" +
                "<div class=\"info-box\">" +
                "<p><strong>Mã lịch khám:</strong> {{appointmentCode}}</p>" +
                "<p><strong>Thời gian cũ:</strong> <del>{{oldTime}}</del></p>" +
                "<p><strong style=\"color:#0056b3;\"> Thời gian MỚI:</strong> {{newTime}}</p>" +
                "</div>" +
                "<a href=\"{{magicLink}}\" class=\"button\">Xem Chi Tiết</a>" +
                "<p>Nếu bạn có thắc mắc về sự thay đổi này, vui lòng liên hệ với phòng khám.</p>";
    }

    private static String getAppointmentCancellationBodyContent() {
        return "<p>Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p>Lịch khám của bạn với mã <strong>{{appointmentCode}}</strong> đã bị <strong style=\"color:#d9534f;\">HỦY</strong>.</p>"
                +
                "<p>Nếu đây là sự nhầm lẫn hoặc bạn muốn đặt lại lịch, vui lòng liên hệ với chúng tôi hoặc:</p>" +
                "<a href=\"{{magicLink}}\" class=\"button\" style=\"background:#d9534f;\">Đặt Lại Lịch</a>";
    }

    private static String getAppointmentStatusChangeBodyContent() {
        return "<p>Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p>Trạng thái lịch khám (Mã: <strong>{{appointmentCode}}</strong>) của bạn đã được cập nhật thành:</p>"
                +
                "<div class=\"info-box\">" +
                "<h3 style=\"color:#0056b3;margin:0;\">{{newStatus}}</h3>" +
                "</div>" +
                "<a href=\"{{magicLink}}\" class=\"button\">Theo Dõi Chi Tiết</a>";
    }

    private static String getDefaultBodyContent() {
        return "<p>Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p>{{content}}</p>";
    }

    /**
     * Get the title for a template type (used for building full content from
     * bodyContent)
     */
    public static String getTitleForType(String templateType) {
        switch (templateType) {
            case "OTP":
                return "Mã Xác Thực OTP";
            case "APPOINTMENT_CONFIRMATION":
                return "Xác Nhận Lịch Khám";
            case "APPOINTMENT_PENDING":
                return "Yêu Cầu Đặt Lịch Đã Nhận";
            case "APPOINTMENT_REMINDER":
                return "Nhắc Nhở Lịch Khám";
            case "NEW_APPOINTMENT":
                return "Đặt Lịch Thành Công";
            case "APPOINTMENT_UPDATE":
                return "Thay Đổi Lịch Khám";
            case "APPOINTMENT_CANCELLATION":
                return "Hủy Lịch Khám";
            case "APPOINTMENT_STATUS_CHANGE":
                return "Cập Nhật Trạng Thái";
            default:
                return "Thông Báo";
        }
    }

    /**
     * Check if template type uses red header
     */
    public static boolean usesRedHeader(String templateType) {
        return "APPOINTMENT_CANCELLATION".equals(templateType);
    }
}
