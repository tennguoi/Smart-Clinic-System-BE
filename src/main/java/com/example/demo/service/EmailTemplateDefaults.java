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
                template.put("content", buildFullContent("Yêu Cầu Đặt Lịch Đã Nhận", getAppointmentPendingBodyContent()));
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
                "<div style=\"max-width:600px;margin:40px auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.1)\">"
                +
                "<div style=\"background:linear-gradient(135deg,#0066cc,#0052a3);color:#fff;padding:30px;text-align:center\">"
                +
                "<h1 style=\"margin:0;font-size:28px;font-weight:600\">" + title + "</h1>" +
                "</div>" +
                "<div style=\"padding:30px\">" +
                bodyContent +
                "</div>" +
                getEmailFooter();
    }

    /**
     * Build full email content with red header (for cancellation emails)
     */
    public static String buildFullContentWithRedHeader(String title, String bodyContent) {
        return getEmailHeader() +
                "<div style=\"max-width:600px;margin:40px auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.1)\">"
                +
                "<div style=\"background:linear-gradient(135deg,#dc3545,#c82333);color:#fff;padding:30px;text-align:center\">"
                +
                "<h1 style=\"margin:0;font-size:28px;font-weight:600\">" + title + "</h1>" +
                "</div>" +
                "<div style=\"padding:30px\">" +
                bodyContent +
                "</div>" +
                getEmailFooter();
    }

    private static String getEmailHeader() {
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0\"></head><body style=\"margin:0;padding:0;font-family:Arial,sans-serif;background-color:#f4f7fa;color:#333\">";
    }

    private static String getEmailFooter() {
        return "</div>" +
                "<div style=\"background:#f8f9fa;padding:20px;text-align:center;color:#6c757d;font-size:14px\">" +
                "<p style=\"margin:5px 0\">Trân trọng, <strong>Phòng khám Tai-Mũi-Họng 2CTW</strong></p>" +
                "<p style=\"margin:5px 0\">Email này được gửi tự động, vui lòng không trả lời.</p>" +
                "</div></body></html>";
    }

    // ==================== BODY CONTENT ONLY (for React Quill editing)
    // ====================

    private static String getOtpBodyContent() {
        return "<p style=\"font-size:16px;line-height:1.6;color:#333\">Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<div style=\"background:#f8f9fa;border-left:4px solid #0066cc;padding:20px;margin:20px 0;border-radius:8px\">"
                +
                "<p style=\"font-size:18px;margin:0;color:#0066cc\">Mã OTP của bạn: <strong style=\"font-size:32px;letter-spacing:4px\">{{otp}}</strong></p>"
                +
                "</div>" +
                "<p style=\"font-size:14px;color:#666\">Vui lòng nhập mã này để tiếp tục. Mã OTP có hiệu lực trong vòng <strong>5 phút</strong>.</p>"
                +
                "<p style=\"font-size:14px;color:#dc3545\">⚠️ <strong>Lưu ý:</strong> Không chia sẻ mã này với bất kỳ ai.</p>";
    }

    private static String getAppointmentConfirmationBodyContent() {
        return "<p style=\"font-size:16px;line-height:1.6;color:#333\">Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p style=\"font-size:16px;line-height:1.6;color:#333\">Lịch khám của bạn đã được <strong style=\"color:#28a745\">XÁC NHẬN</strong>!</p>"
                +
                "<div style=\"background:#f8f9fa;padding:20px;margin:20px 0;border-radius:8px;border:2px solid #28a745\">"
                +
                "<p style=\"margin:8px 0;font-size:15px\"><strong>Mã lịch khám:</strong> {{appointmentCode}}</p>" +
                "<p style=\"margin:8px 0;font-size:15px\"><strong>Họ tên:</strong> {{patientName}}</p>" +
                "<p style=\"margin:8px 0;font-size:15px\"><strong>Thời gian:</strong> {{appointmentTime}}</p>" +
                "<p style=\"margin:8px 0;font-size:15px\"><strong>Được xác nhận bởi:</strong> {{confirmedByName}}</p>"
                +
                "</div>" +
                "<p style=\"font-size:14px;color:#666\">Vui lòng đến đúng giờ. Nếu có thay đổi, vui lòng liên hệ phòng khám.</p>";
    }

    private static String getAppointmentPendingBodyContent() {
        return "<p style=\"font-size:16px;line-height:1.6;color:#333\">Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p style=\"font-size:16px;line-height:1.6;color:#333\">Chúng tôi đã nhận được yêu cầu đặt lịch khám của bạn.</p>"
                +
                "<div style=\"background:#fff3cd;padding:20px;margin:20px 0;border-radius:8px;border-left:4px solid #ffc107\">"
                +
                "<p style=\"margin:8px 0;font-size:15px\"><strong>Mã lịch khám:</strong> {{appointmentCode}}</p>" +
                "<p style=\"margin:8px 0;font-size:15px\"><strong>Họ tên:</strong> {{patientName}}</p>" +
                "<p style=\"margin:8px 0;font-size:15px\"><strong>Thời gian dự kiến:</strong> {{appointmentTime}}</p>"
                +
                "<p style=\"margin:8px 0;font-size:15px\"><strong>Số điện thoại:</strong> {{phone}}</p>" +
                "<p style=\"margin:8px 0;font-size:15px\"><strong>Dịch vụ đăng ký:</strong> {{serviceNames}}</p>" +
                "<p style=\"margin:8px 0;font-size:15px\"><strong>Triệu chứng/Ghi chú:</strong> {{notes}}</p>" +
                "</div>" +
                "<p style=\"font-size:14px;color:#666\">Nhân viên phòng khám sẽ sớm liên hệ với bạn để xác nhận lịch.</p>";
    }

    private static String getAppointmentReminderBodyContent() {
        return "<p style=\"font-size:16px;line-height:1.6;color:#333\">Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p style=\"font-size:16px;line-height:1.6;color:#333\">Đây là email nhắc nhở lịch khám của bạn vào <strong style=\"color:#dc3545\">NGÀY MAI</strong>.</p>"
                +
                "<div style=\"background:#e7f3ff;padding:20px;margin:20px 0;border-radius:8px;border-left:4px solid #0066cc\">"
                +
                "<p style=\"margin:8px 0;font-size:15px\"><strong>Mã lịch khám:</strong> {{appointmentCode}}</p>" +
                "<p style=\"margin:8px 0;font-size:15px\"><strong>Họ tên:</strong> {{patientName}}</p>" +
                "<p style=\"margin:8px 0;font-size:15px\"><strong>Thời gian:</strong> {{appointmentTime}}</p>" +
                "<p style=\"margin:8px 0;font-size:15px\"><strong>Thời gian còn lại:</strong> Khoảng <strong style=\"color:#dc3545\">{{hoursUntilAppointment}} giờ nữa</strong></p>"
                +
                "</div>" +
                "<div style=\"background:#fff;border:2px dashed #0066cc;padding:20px;margin:20px 0;border-radius:8px\">" +
                "<p style=\"margin:0 0 10px 0;font-size:16px;font-weight:600;color:#0066cc\">📋 Lưu ý quan trọng:</p>"
                +
                "<ul style=\"margin:0;padding-left:20px;color:#333\">" +
                "<li style=\"margin:8px 0\">Đến sớm <strong>15 phút</strong> trước giờ hẹn</li>" +
                "<li style=\"margin:8px 0\">Mang theo giấy tờ tùy thân (CMND/CCCD)</li>" +
                "<li style=\"margin:8px 0\">Nếu không thể đến, vui lòng liên hệ sớm nhất</li>" +
                "</ul>" +
                "</div>" +
                "<p style=\"font-size:14px;color:#666;margin-top:20px\">📧 <strong>Email:</strong> {{clinicEmail}}</p>";
    }

    private static String getNewAppointmentBodyContent() {
        return "<p style=\"font-size:16px;line-height:1.6;color:#333\">Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p style=\"font-size:16px;line-height:1.6;color:#333\">Cảm ơn bạn đã đặt lịch khám tại phòng khám của chúng tôi.</p>"
                +
                "<div style=\"background:#f8f9fa;padding:20px;margin:20px 0;border-radius:8px;border-left:4px solid #0066cc\">"
                +
                "<p style=\"margin:8px 0;font-size:15px\"><strong>Mã lịch hẹn:</strong> {{appointmentCode}}</p>" +
                "<p style=\"margin:8px 0;font-size:15px\"><strong>Họ tên:</strong> {{patientName}}</p>" +
                "<p style=\"margin:8px 0;font-size:15px\"><strong>Thời gian:</strong> {{appointmentTime}}</p>" +
                "<p style=\"margin:8px 0;font-size:15px\"><strong>Dịch vụ đăng ký:</strong> {{serviceNames}}</p>" +
                "<p style=\"margin:8px 0;font-size:15px\"><strong>Ghi chú:</strong> {{notes}}</p>" +
                "</div>" +
                "<div style=\"text-align:center;margin:30px 0\">" +
                "<a href=\"{{trackingLink}}\" style=\"display:inline-block;background:#0066cc;color:#ffffff;text-decoration:none;padding:14px 32px;border-radius:8px;font-weight:600;font-size:16px\">Theo Dõi Lịch Khám</a>"
                +
                "</div>" +
                "<p style=\"font-size:14px;color:#666\">Vui lòng đến đúng giờ để được phục vụ tốt nhất.</p>";
    }

    private static String getAppointmentUpdateBodyContent() {
        return "<p style=\"font-size:16px;line-height:1.6;color:#333\">Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p style=\"font-size:16px;line-height:1.6;color:#333\">Lịch khám của bạn đã được <strong style=\"color:#ffc107\">THAY ĐỔI</strong>.</p>"
                +
                "<div style=\"background:#fff3cd;padding:20px;margin:20px 0;border-radius:8px;border-left:4px solid #ffc107\">"
                +
                "<p style=\"margin:8px 0;font-size:15px\"><strong>Mã lịch khám:</strong> {{appointmentCode}}</p>" +
                "<p style=\"margin:8px 0;font-size:15px\"><strong>Thời gian cũ:</strong> <span style=\"text-decoration:line-through;color:#999\">{{oldTime}}</span></p>"
                +
                "<p style=\"margin:8px 0;font-size:15px\"><strong>Thời gian MỚI:</strong> <span style=\"color:#dc3545;font-weight:600\">{{newTime}}</span></p>"
                +
                "</div>" +
                "<div style=\"text-align:center;margin:30px 0\">" +
                "<a href=\"{{detailLink}}\" style=\"display:inline-block;background:#ffc107;color:#000000;text-decoration:none;padding:14px 32px;border-radius:8px;font-weight:600;font-size:16px\">Xem Chi Tiết</a>"
                +
                "</div>" +
                "<p style=\"font-size:14px;color:#666\">Nếu bạn có thắc mắc về sự thay đổi này, vui lòng liên hệ với phòng khám.</p>";
    }

    private static String getAppointmentCancellationBodyContent() {
        return "<p style=\"font-size:16px;line-height:1.6;color:#333\">Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p style=\"font-size:16px;line-height:1.6;color:#333\">Lịch khám của bạn với mã <strong>{{appointmentCode}}</strong> đã bị <strong style=\"color:#dc3545\">HỦY</strong>.</p>"
                +
                "<p style=\"font-size:14px;color:#666\">Nếu đây là sự nhầm lẫn hoặc bạn muốn đặt lại lịch, vui lòng liên hệ với chúng tôi hoặc:</p>"
                +
                "<div style=\"text-align:center;margin:30px 0\">" +
                "<a href=\"{{rebookLink}}\" style=\"display:inline-block;background:#0066cc;color:#ffffff;text-decoration:none;padding:14px 32px;border-radius:8px;font-weight:600;font-size:16px\">Đặt Lại Lịch</a>"
                +
                "</div>";
    }

    private static String getAppointmentStatusChangeBodyContent() {
        return "<p style=\"font-size:16px;line-height:1.6;color:#333\">Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p style=\"font-size:16px;line-height:1.6;color:#333\">Trạng thái lịch khám (Mã: <strong>{{appointmentCode}}</strong>) của bạn đã được cập nhật thành:</p>"
                +
                "<div style=\"background:#e7f3ff;padding:20px;margin:20px 0;border-radius:8px;text-align:center\">" +
                "<p style=\"font-size:24px;font-weight:600;color:#0066cc;margin:0\">{{newStatus}}</p>" +
                "</div>" +
                "<div style=\"text-align:center;margin:30px 0\">" +
                "<a href=\"{{detailLink}}\" style=\"display:inline-block;background:#0066cc;color:#ffffff;text-decoration:none;padding:14px 32px;border-radius:8px;font-weight:600;font-size:16px\">Theo Dõi Chi Tiết</a>"
                +
                "</div>";
    }

    private static String getDefaultBodyContent() {
        return "<p style=\"font-size:16px;line-height:1.6;color:#333\">Kính gửi <strong>{{patientName}}</strong>,</p>" +
                "<p style=\"font-size:16px;line-height:1.6;color:#333\">{{content}}</p>";
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