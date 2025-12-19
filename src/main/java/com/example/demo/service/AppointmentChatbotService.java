package com.example.demo.service;

import com.example.demo.dto.appointment.AppointmentChatbotRequest;
import com.example.demo.dto.appointment.AppointmentChatbotResponse;
import com.example.demo.entity.Appointment;
import com.example.demo.repository.AppointmentChatbotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentChatbotService {

    private final AppointmentChatbotRepository repository;
    private final EmailService emailService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional
    public AppointmentChatbotResponse createAppointment(AppointmentChatbotRequest request) {
        try {
            // 1. Parse thời gian khám
            LocalDateTime appointmentTime = parseAppointmentTime(request.getAppointmentTime());

            // 2. Validate thời gian khám
            validateAppointmentTime(appointmentTime);

            // 3. Validate số điện thoại
            validatePhone(request.getPhone());

            // 4. Parse ngày sinh (nếu có)
            LocalDate dob = null;
            if (request.getDob() != null && !request.getDob().trim().isEmpty()) {
                try {
                    dob = LocalDate.parse(request.getDob().trim(), DATE_FORMATTER);
                    validateDob(dob);
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Định dạng ngày sinh không đúng. Vui lòng dùng: YYYY-MM-DD");
                }
            }

            // 5. Validate email (nếu có) - chỉ validate format, không bắt buộc
            String validatedEmail = null;
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                validateEmail(request.getEmail().trim());
                validatedEmail = request.getEmail().trim();
            }

            // 6. Validate CCCD (nếu có)
            if (request.getIdNumber() != null && !request.getIdNumber().trim().isEmpty()) {
                validateIdNumber(request.getIdNumber().trim());
            }

            // 7. Tạo appointment code
            String appointmentCode = generateAppointmentCode(appointmentTime);

            // 8. Tạo appointment entity
            Appointment appointment = Appointment.builder()
                    .patientName(request.getPatientName().trim())
                    .phone(request.getPhone().trim())
                    .email(validatedEmail)
                    .dob(dob)
                    .idNumber(request.getIdNumber() != null ? request.getIdNumber().trim() : null)
                    .appointmentTime(appointmentTime)
                    .appointmentCode(appointmentCode)
                    .notes(request.getNotes() != null ? request.getNotes().trim() : null)
                    .quickBook(false) // Chatbot không phải quick book
                    .build();

            // 9. Lưu vào database
            Appointment saved = repository.save(appointment);

            log.info("✅ Đặt lịch qua chatbot thành công: {} - {}", saved.getAppointmentCode(), saved.getPatientName());

            // 10. Gửi email thông báo (chỉ khi có email hợp lệ)
            boolean emailSent = false;
            if (saved.getEmail() != null && !saved.getEmail().trim().isEmpty()) {
                emailSent = sendEmailNotification(saved);
            }

            // 11. Tạo thông báo response
            String successMessage = "Đặt lịch thành công!";
            if (saved.getEmail() != null && !saved.getEmail().trim().isEmpty()) {
                if (emailSent) {
                    successMessage += " Email xác nhận đã được gửi đến " + saved.getEmail();
                } else {
                    successMessage += " (Không thể gửi email xác nhận, vui lòng kiểm tra lại địa chỉ email)";
                }
            }

            // 12. Trả response
            return AppointmentChatbotResponse.builder()
                    .success(true)
                    .message(successMessage)
                    .appointmentId(saved.getAppointmentId())
                    .appointmentCode(saved.getAppointmentCode())
                    .patientName(saved.getPatientName())
                    .phone(saved.getPhone())
                    .email(saved.getEmail())
                    .dob(saved.getDob() != null ? saved.getDob().toString() : null)
                    .idNumber(saved.getIdNumber())
                    .appointmentTime(saved.getAppointmentTime().format(DATE_TIME_FORMATTER))
                    .status(saved.getStatus().name())
                    .notes(saved.getNotes())
                    .build();

        } catch (DateTimeParseException e) {
            log.error("❌ Lỗi format thời gian: {}", request.getAppointmentTime());
            throw new IllegalArgumentException("Định dạng thời gian không đúng. Vui lòng dùng: YYYY-MM-DD HH:mm");
        } catch (Exception e) {
            log.error("❌ Lỗi tạo lịch hẹn", e);
            throw e;
        }
    }

    /**
     * Gửi email thông báo đặt lịch thành công
     * @return true nếu gửi thành công, false nếu có lỗi
     */
    private boolean sendEmailNotification(Appointment appointment) {
        try {
            // Double check email có hợp lệ không
            if (appointment.getEmail() == null || appointment.getEmail().trim().isEmpty()) {
                log.debug("⏭️ Bỏ qua gửi email: không có địa chỉ email");
                return false;
            }

            // Tạo magic link để tracking
            String magicLink = "http://localhost:5173/appointments/tracking?code=" +
                    appointment.getAppointmentCode();

            // Gửi email với danh sách dịch vụ rỗng (chatbot không chọn dịch vụ)
            emailService.sendNewAppointmentNotification(
                    appointment.getEmail(),
                    appointment.getPatientName(),
                    appointment.getAppointmentCode(),
                    appointment.getAppointmentTime(),
                    appointment.getNotes(),
                    new ArrayList<>(), // Danh sách dịch vụ rỗng
                    magicLink
            );

            log.info("✅ Email xác nhận đặt lịch đã được gửi đến: {}", appointment.getEmail());
            return true;

        } catch (Exception e) {
            // Log lỗi nhưng không throw exception để không làm fail toàn bộ process
            log.error("⚠️ Không thể gửi email đến {}: {}", appointment.getEmail(), e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private LocalDateTime parseAppointmentTime(String timeStr) {
        try {
            return LocalDateTime.parse(timeStr, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Định dạng thời gian không đúng. Vui lòng dùng: YYYY-MM-DD HH:mm (ví dụ: 2025-11-20 09:30)"
            );
        }
    }

    private void validatePhone(String phone) {
        String cleanPhone = phone.replaceAll("\\s+", "");
        if (!cleanPhone.matches("\\d{10}")) {
            throw new IllegalArgumentException("Số điện thoại phải có đúng 10 chữ số");
        }
    }

    private void validateEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            throw new IllegalArgumentException("Email không hợp lệ");
        }
    }

    private void validateIdNumber(String idNumber) {
        String cleanId = idNumber.replaceAll("\\s+", "");
        if (!cleanId.matches("\\d{9}") && !cleanId.matches("\\d{12}")) {
            throw new IllegalArgumentException("Số CCCD phải có 9 hoặc 12 chữ số");
        }
    }

    private void validateDob(LocalDate dob) {
        LocalDate today = LocalDate.now();
        if (dob.isAfter(today)) {
            throw new IllegalArgumentException("Ngày sinh không được là ngày trong tương lai");
        }

        LocalDate minDate = today.minusYears(150);
        if (dob.isBefore(minDate)) {
            throw new IllegalArgumentException("Ngày sinh không hợp lệ");
        }
    }

    private String generateAppointmentCode(LocalDateTime appointmentTime) {
        String datePart = appointmentTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timePart = appointmentTime.format(DateTimeFormatter.ofPattern("HHmm"));
        String randomPart = String.format("%03d", (int)(Math.random() * 1000));
        return "APPT-" + datePart + "-" + timePart + "-" + randomPart;
    }

    private void validateAppointmentTime(LocalDateTime appointmentTime) {
        LocalDate today = LocalDate.now();
        LocalDate appointmentDate = appointmentTime.toLocalDate();

        if (appointmentDate.isBefore(today)) {
            throw new IllegalArgumentException("Ngày khám không được là ngày trong quá khứ");
        }

        if (appointmentDate.isEqual(today)) {
            LocalDateTime now = LocalDateTime.now();
            if (appointmentTime.isBefore(now)) {
                throw new IllegalArgumentException("Thời gian khám phải là thời điểm trong tương lai");
            }
        }

        LocalDate sevenDaysLater = today.plusDays(7);
        if (appointmentDate.isAfter(sevenDaysLater)) {
            throw new IllegalArgumentException("Chỉ có thể đặt lịch từ hôm nay đến 7 ngày tiếp theo");
        }

        java.time.LocalTime time = appointmentTime.toLocalTime();
        java.time.LocalTime morningStart = java.time.LocalTime.of(8, 0);
        java.time.LocalTime morningEnd = java.time.LocalTime.of(12, 0);
        java.time.LocalTime afternoonStart = java.time.LocalTime.of(14, 0);
        java.time.LocalTime afternoonEnd = java.time.LocalTime.of(18, 0);

        boolean isWorkingHours =
                (time.compareTo(morningStart) >= 0 && time.isBefore(morningEnd)) ||
                        (time.compareTo(afternoonStart) >= 0 && time.isBefore(afternoonEnd));

        if (!isWorkingHours) {
            throw new IllegalArgumentException(
                    "Giờ khám phải trong khung làm việc: Sáng 8:00-12:00 hoặc Chiều 14:00-18:00"
            );
        }
    }
}