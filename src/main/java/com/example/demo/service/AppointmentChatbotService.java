package com.example.demo.service;

import com.example.demo.dto.AppointmentChatbotRequest;
import com.example.demo.dto.AppointmentChatbotResponse;
import com.example.demo.entity.Appointment;
import com.example.demo.repository.AppointmentChatbotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentChatbotService {

    private final AppointmentChatbotRepository repository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Transactional
    public AppointmentChatbotResponse createAppointment(AppointmentChatbotRequest request) {
        try {
            // 1. Parse thời gian
            LocalDateTime appointmentTime = parseAppointmentTime(request.getAppointmentTime());

            // 2. Validate
            validateAppointmentTime(appointmentTime);

            // 3. Validate số điện thoại
            validatePhone(request.getPhone());

            // 4. Tạo appointment code
            String appointmentCode = generateAppointmentCode(appointmentTime);

            // 5. Tạo appointment entity
            Appointment appointment = Appointment.builder()
                    .patientName(request.getPatientName().trim())
                    .phone(request.getPhone().trim())
                    .appointmentTime(appointmentTime)
                    .appointmentCode(appointmentCode)
                    .notes(request.getNotes() != null ? request.getNotes().trim() : null)
                    .build();

            // 6. Lưu vào database
            Appointment saved = repository.save(appointment);

            log.info("✅ Đặt lịch thành công: {} - {}", saved.getAppointmentCode(), saved.getPatientName());

            // 7. Trả response
            return AppointmentChatbotResponse.builder()
                    .success(true)
                    .message("Đặt lịch thành công")
                    .appointmentId(saved.getAppointmentId())
                    .appointmentCode(saved.getAppointmentCode())
                    .patientName(saved.getPatientName())
                    .phone(saved.getPhone())
                    .appointmentTime(saved.getAppointmentTime().format(FORMATTER))
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

    // Parse thời gian từ string
    private LocalDateTime parseAppointmentTime(String timeStr) {
        try {
            return LocalDateTime.parse(timeStr, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Định dạng thời gian không đúng. Vui lòng dùng: YYYY-MM-DD HH:mm (ví dụ: 2025-11-20 09:30)"
            );
        }
    }

    // Validate số điện thoại - chỉ cần 10 chữ số
    private void validatePhone(String phone) {
        String cleanPhone = phone.replaceAll("\\s+", ""); // Xóa khoảng trắng

        if (!cleanPhone.matches("\\d{10}")) {
            throw new IllegalArgumentException("Số điện thoại phải có đúng 10 chữ số");
        }
    }

    // Generate appointment code: APPT-YYYYMMDD-HHMM-XXX
    private String generateAppointmentCode(LocalDateTime appointmentTime) {
        String datePart = appointmentTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timePart = appointmentTime.format(DateTimeFormatter.ofPattern("HHmm"));
        String randomPart = String.format("%03d", (int)(Math.random() * 1000));
        return "APPT-" + datePart + "-" + timePart + "-" + randomPart;
    }

    // Validate logic nghiệp vụ
    private void validateAppointmentTime(LocalDateTime appointmentTime) {
        LocalDate today = LocalDate.now();
        LocalDate appointmentDate = appointmentTime.toLocalDate();

        // 1. Kiểm tra ngày khám phải từ hôm nay trở đi (không được quá khứ)
        if (appointmentDate.isBefore(today)) {
            throw new IllegalArgumentException("Ngày khám không được là ngày trong quá khứ");
        }

        // 2. Nếu là hôm nay, kiểm tra giờ phải trong tương lai
        if (appointmentDate.isEqual(today)) {
            LocalDateTime now = LocalDateTime.now();
            if (appointmentTime.isBefore(now)) {
                throw new IllegalArgumentException("Thời gian khám phải là thời điểm trong tương lai");
            }
        }

        // 3. Kiểm tra chỉ được đặt lịch trong vòng 7 ngày tính từ hôm nay
        LocalDate sevenDaysLater = today.plusDays(7);
        if (appointmentDate.isAfter(sevenDaysLater)) {
            throw new IllegalArgumentException("Chỉ có thể đặt lịch từ hôm nay đến 7 ngày tiếp theo");
        }

        // 4. Kiểm tra giờ làm việc: 8:00-12:00 hoặc 14:00-18:00
        LocalTime time = appointmentTime.toLocalTime();
        LocalTime morningStart = LocalTime.of(8, 0);
        LocalTime morningEnd = LocalTime.of(12, 0);
        LocalTime afternoonStart = LocalTime.of(14, 0);
        LocalTime afternoonEnd = LocalTime.of(18, 0);

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