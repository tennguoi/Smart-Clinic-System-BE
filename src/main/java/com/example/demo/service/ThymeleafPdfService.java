package com.example.demo.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.example.demo.dto.MedicalRecordDetailResponse;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

@Service
public class ThymeleafPdfService {
    private final SpringTemplateEngine templateEngine;
    private final MedicalRecordService medicalRecordService;

    public ThymeleafPdfService(SpringTemplateEngine templateEngine, MedicalRecordService medicalRecordService) {
        this.templateEngine = templateEngine;
        this.medicalRecordService = medicalRecordService;
    }

    public byte[] generatePdfForRecord(UUID recordId) {
        MedicalRecordDetailResponse detail = medicalRecordService.getRecordDetail(recordId);
        Context ctx = new Context();
        ctx.setVariable("record", detail);
        ctx.setVariable("prescription", detail.getPrescription());

        String html = templateEngine.process("medical-record-print", ctx);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);

            // Try to register an embedded font from resources/fonts
            try (InputStream is = getClass().getResourceAsStream("/static/NotoSans-Regular.ttf")) {
                if (is != null) {
                    File tmp = File.createTempFile("font-dejavu", ".ttf");
                    tmp.deleteOnExit();
                    Files.copy(is, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    builder.useFont(tmp, "DejaVu Sans");
                }
            } catch (Exception fe) {
                // ignore font load problems
            }

            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo PDF từ template: " + e.getMessage(), e);
        }
    }
}
