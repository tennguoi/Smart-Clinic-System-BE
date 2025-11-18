package com.example.demo.service;

import com.example.demo.entity.MedicalRecord;
import com.example.demo.entity.Prescription;
import com.example.demo.repository.PrescriptionRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

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
    
    private void addTableRow(Table table, String label, String value) {
        Cell labelCell = new Cell();
        labelCell.add(new Paragraph(label).setBold());
        table.addCell(labelCell);
        
        Cell valueCell = new Cell();
        valueCell.add(new Paragraph(value));
        table.addCell(valueCell);
    }
}
