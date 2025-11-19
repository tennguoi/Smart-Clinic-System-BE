package com.example.demo.service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.entity.MedicalRecord;
import com.example.demo.entity.Prescription;
import com.example.demo.repository.PrescriptionRepository;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

@Service
public class PdfExportService {
    private final PrescriptionRepository prescriptionRepository;

    public PdfExportService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    public byte[] generateMedicalRecordPdf(MedicalRecord record) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Try to load an embedded font that supports Vietnamese (UTF-8).
            // Place a TTF file under `src/main/resources/fonts/` (e.g. NotoSans-Regular.ttf or DejaVuSans.ttf).
            try {
                PdfFont font = null;
                try (java.io.InputStream is = getClass().getResourceAsStream("/static/NotoSans-Regular.ttf")) {
                    if (is != null) {
                        byte[] fontBytes = is.readAllBytes();
                        font = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                    }
                }
                if (font == null) {
                    font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                }
                document.setFont(font);
            } catch (Exception fe) {
                // If font loading fails, continue with default font (may not render Vietnamese correctly).
            }
            
            // Tiêu đề
            Paragraph title = new Paragraph("HỒ SƠ BỆNH ÁN")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);
            
            Paragraph subtitle = new Paragraph("PHÒNG KHÁM ĐA KHOA HEALTHCARE")
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(subtitle);
            
            document.add(new Paragraph("\n"));
            
            // Thông tin bệnh nhân
            Paragraph patientSection = new Paragraph("THÔNG TIN BỆNH NHÂN")
                    .setFontSize(12)
                    .setBold();
            document.add(patientSection);
            
            Table patientTable = new Table(2);
            patientTable.setWidth(UnitValue.createPercentValue(100));
            
            addTableRow(patientTable, "Tên bệnh nhân:", 
                    record.getPatientName() != null ? record.getPatientName() : 
                    (record.getPatient() != null ? record.getPatient().getFullName() : "N/A"));
            addTableRow(patientTable, "Bác sĩ khám:", 
                    record.getDoctor() != null ? record.getDoctor().getFullName() : "N/A");
            addTableRow(patientTable, "Ngày khám:", 
                    record.getCreatedAt() != null ? 
                    record.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A");
            
            document.add(patientTable);
            document.add(new Paragraph("\n"));
            
            // Chẩn đoán
            Paragraph diagnosisSection = new Paragraph("CHẨN ĐOÁN")
                    .setFontSize(12)
                    .setBold();
            document.add(diagnosisSection);
            
            Paragraph diagnosis = new Paragraph(record.getDiagnosis() != null ? record.getDiagnosis() : "N/A")
                    .setFontSize(11);
            document.add(diagnosis);
            document.add(new Paragraph("\n"));
            
            // Ghi chú điều trị
            Paragraph treatmentSection = new Paragraph("GHI CHÚ ĐIỀU TRỊ")
                    .setFontSize(12)
                    .setBold();
            document.add(treatmentSection);
            
            Paragraph treatment = new Paragraph(record.getTreatmentNotes() != null ? record.getTreatmentNotes() : "N/A")
                    .setFontSize(11);
            document.add(treatment);
            document.add(new Paragraph("\n"));
            
            // Đơn thuốc
            Optional<Prescription> prescription = prescriptionRepository.findByRecordId(record.getRecordId());
            
            if (prescription.isPresent()) {
                Paragraph prescriptionSection = new Paragraph("ĐƠN THUỐC CHỈ ĐỊNH")
                        .setFontSize(12)
                        .setBold();
                document.add(prescriptionSection);
                
                Prescription rx = prescription.get();
                
                // Bảng thuốc
                Table drugTable = new Table(1);
                drugTable.setWidth(UnitValue.createPercentValue(100));
                
                Cell drugHeader = new Cell();
                drugHeader.add(new Paragraph("DANH SÁCH THUỐC").setBold());
                drugTable.addCell(drugHeader);
                
                Cell drugContent = new Cell();
                drugContent.add(new Paragraph(rx.getDrugs() != null ? rx.getDrugs() : "N/A"));
                drugTable.addCell(drugContent);
                
                document.add(drugTable);
                document.add(new Paragraph("\n"));
                
                // Hướng dẫn sử dụng
                Paragraph instructionSection = new Paragraph("HƯỚNG DẪN SỬ DỤNG")
                        .setFontSize(12)
                        .setBold();
                document.add(instructionSection);
                
                Paragraph instructions = new Paragraph(rx.getInstructions() != null ? rx.getInstructions() : "N/A")
                        .setFontSize(11);
                document.add(instructions);
                document.add(new Paragraph("\n"));
                
                // Ngày kê đơn
                Paragraph issuedDate = new Paragraph("Ngày kê đơn: " + 
                    (rx.getIssuedAt() != null ? 
                    rx.getIssuedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A"))
                        .setFontSize(10);
                document.add(issuedDate);
            }
            
            document.add(new Paragraph("\n\n"));
            
            // Footer
            Paragraph footer = new Paragraph("Tài liệu này được tạo tự động từ hệ thống quản lý bệnh án")
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic();
            document.add(footer);
            
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo PDF: " + e.getMessage(), e);
        }
    }

    public byte[] generatePrescriptionPdf(Prescription rx) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // reuse the same font-loading logic
            try {
                PdfFont font = null;
                try (java.io.InputStream is = getClass().getResourceAsStream("/fonts/NotoSans-Regular.ttf")) {
                    if (is != null) {
                        byte[] fontBytes = is.readAllBytes();
                        font = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                    }
                }
                if (font == null) {
                    try (java.io.InputStream is2 = getClass().getResourceAsStream("/fonts/DejaVuSans.ttf")) {
                        if (is2 != null) {
                            byte[] fontBytes2 = is2.readAllBytes();
                            font = PdfFontFactory.createFont(fontBytes2, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                        }
                    }
                }
                if (font == null) {
                    font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                }
                document.setFont(font);
            } catch (Exception fe) {
                // ignore font failure
            }

            Paragraph title = new Paragraph("ĐƠN THUỐC CHỈ ĐỊNH")
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            document.add(new Paragraph("\n"));

            Table info = new Table(UnitValue.createPercentArray(new float[]{1, 2}));
            info.setWidth(UnitValue.createPercentValue(100));
            Cell c1 = new Cell();
            c1.add(new Paragraph("Bệnh nhân:").setBold());
            info.addCell(c1);
            Cell c2 = new Cell();
            String patientName = rx.getMedicalRecord() != null && rx.getMedicalRecord().getPatient() != null
                    ? rx.getMedicalRecord().getPatient().getFullName() : "N/A";
            c2.add(new Paragraph(patientName));
            info.addCell(c2);

            Cell c3 = new Cell();
            c3.add(new Paragraph("Bác sĩ:").setBold());
            info.addCell(c3);
            Cell c4 = new Cell();
            String doctorName = rx.getMedicalRecord() != null && rx.getMedicalRecord().getDoctor() != null
                    ? rx.getMedicalRecord().getDoctor().getFullName() : "N/A";
            c4.add(new Paragraph(doctorName));
            info.addCell(c4);

            Cell c5 = new Cell();
            c5.add(new Paragraph("Ngày kê đơn:").setBold());
            info.addCell(c5);
            Cell c6 = new Cell();
            c6.add(new Paragraph(rx.getIssuedAt() != null ? rx.getIssuedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A"));
            info.addCell(c6);

            document.add(info);
            document.add(new Paragraph("\n"));

            Paragraph drugsHeader = new Paragraph("Danh sách thuốc").setBold().setFontSize(12);
            document.add(drugsHeader);
            Table drugsTable = new Table(1);
            drugsTable.setWidth(UnitValue.createPercentValue(100));
            Cell drugsCell = new Cell();
            drugsCell.add(new Paragraph(rx.getDrugs() != null ? rx.getDrugs() : "N/A"));
            drugsTable.addCell(drugsCell);
            document.add(drugsTable);

            document.add(new Paragraph("\n"));
            Paragraph instrHeader = new Paragraph("Hướng dẫn sử dụng").setBold().setFontSize(12);
            document.add(instrHeader);
            Paragraph instr = new Paragraph(rx.getInstructions() != null ? rx.getInstructions() : "N/A").setFontSize(11);
            document.add(instr);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo PDF đơn thuốc: " + e.getMessage(), e);
        }
    }

    public byte[] generatePrescriptionPdfForRecord(MedicalRecord record) {
        Optional<Prescription> opt = prescriptionRepository.findByRecordId(record.getRecordId());
        Prescription rx = opt.orElseThrow(() -> new RuntimeException("Không tìm thấy đơn thuốc cho hồ sơ."));
        return generatePrescriptionPdf(rx);
    }
    
    private void addTableRow(Table table, String label, String value) {
        Cell labelCell = new Cell();
        labelCell.add(new Paragraph(label).setBold());
        table.addCell(labelCell);
        
        Cell valueCell = new Cell();
        valueCell.add(new Paragraph(value));
        table.addCell(valueCell);
    }
}