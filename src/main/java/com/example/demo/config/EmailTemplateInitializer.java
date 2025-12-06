package com.example.demo.config;

import com.example.demo.entity.EmailTemplate;
import com.example.demo.repository.EmailTemplateRepository;
import com.example.demo.service.EmailTemplateDefaults;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class EmailTemplateInitializer implements CommandLineRunner {

    @Autowired
    private EmailTemplateRepository emailTemplateRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        initializeTemplates();
    }

    private void initializeTemplates() {
        // Define all template types with their metadata
        List<TemplateDefinition> definitions = Arrays.asList(
                new TemplateDefinition("OTP", "Mã OTP",
                        Arrays.asList("patientName", "otp", "logoUrl")),
                new TemplateDefinition("APPOINTMENT_CONFIRMATION", "Xác Nhận Lịch Khám",
                        Arrays.asList("patientName", "appointmentCode", "appointmentTime", "confirmedByName",
                                "logoUrl")),
                new TemplateDefinition("APPOINTMENT_PENDING", "Yêu Cầu Đặt Lịch",
                        Arrays.asList("patientName", "appointmentCode", "appointmentTime", "phone", "serviceNames",
                                "notes", "logoUrl")),
                new TemplateDefinition("APPOINTMENT_REMINDER", "Nhắc Nhở Lịch Khám",
                        Arrays.asList("patientName", "appointmentCode", "appointmentTime", "hoursUntilAppointment",
                                "clinicEmail", "logoUrl")),
                new TemplateDefinition("NEW_APPOINTMENT", "Đặt Lịch Thành Công",
                        Arrays.asList("patientName", "appointmentCode", "appointmentTime", "serviceNames", "notes",
                                "magicLink", "logoUrl")),
                new TemplateDefinition("APPOINTMENT_UPDATE", "Thay Đổi Lịch Khám",
                        Arrays.asList("patientName", "appointmentCode", "oldTime", "newTime", "magicLink", "logoUrl")),
                new TemplateDefinition("APPOINTMENT_CANCELLATION", "Hủy Lịch Khám",
                        Arrays.asList("patientName", "appointmentCode", "magicLink", "logoUrl", "cancelReason")),
                new TemplateDefinition("APPOINTMENT_STATUS_CHANGE", "Cập Nhật Trạng Thái",
                        Arrays.asList("patientName", "appointmentCode", "newStatus", "magicLink", "logoUrl")));

        for (TemplateDefinition def : definitions) {
            if (!emailTemplateRepository.existsByTemplateType(def.type)) {
                Map<String, String> defaults = EmailTemplateDefaults.getDefault(def.type);

                try {
                    EmailTemplate template = EmailTemplate.builder()
                            .templateType(def.type)
                            .templateName(def.name)
                            .subject(defaults.get("subject"))
                            .content(defaults.get("content"))
                            .isHtml(true)
                            .availablePlaceholders(objectMapper.writeValueAsString(def.placeholders))
                            .build();

                    emailTemplateRepository.save(template);
                    System.out.println("✓ Initialized email template: " + def.name);
                } catch (Exception e) {
                    System.err.println("✗ Failed to initialize template: " + def.name);
                    e.printStackTrace();
                }
            }
        }
    }

    private static class TemplateDefinition {
        String type;
        String name;
        List<String> placeholders;

        TemplateDefinition(String type, String name, List<String> placeholders) {
            this.type = type;
            this.name = name;
            this.placeholders = placeholders;
        }
    }
}
