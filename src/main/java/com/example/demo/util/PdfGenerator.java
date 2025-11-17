package com.example.demo.util;

import com.example.demo.entity.MedicalRecord;
import com.example.demo.entity.Patient;
import com.example.demo.entity.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

public class PdfGenerator {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public static byte[] generateMedicalRecordPDF(MedicalRecord medicalRecord) throws IOException, DocumentException {
        Patient patient = medicalRecord.getPatient();
        User doctor = medicalRecord.getDoctor();
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        
        document.open();
        
        // Font setup
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        
        // Title
        Paragraph title = new Paragraph("HỒ SƠ BỆNH ÁN", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);
        
        // Patient Information Section
        Paragraph patientHeader = new Paragraph("THÔNG TIN BỆNH NHÂN", headerFont);
        document.add(patientHeader);
        
        PdfPTable patientTable = new PdfPTable(2);
        patientTable.setWidthPercentage(100);
        patientTable.setSpacingBefore(10);
        patientTable.setSpacingAfter(10);
        
        addTableRow(patientTable, "Họ và tên:", patient.getFullName(), boldFont, normalFont);
        addTableRow(patientTable, "Tuổi:", calculateAge(patient.getDateOfBirth()), boldFont, normalFont);
        addTableRow(patientTable, "Giới tính:", getGenderDisplay(patient.getGender()), boldFont, normalFont);
        addTableRow(patientTable, "Ngày sinh:", formatDate(patient.getDateOfBirth()), boldFont, normalFont);
        addTableRow(patientTable, "Địa chỉ:", patient.getAddress(), boldFont, normalFont);
        addTableRow(patientTable, "Điện thoại:", patient.getPhone(), boldFont, normalFont);
        addTableRow(patientTable, "Email:", patient.getEmail(), boldFont, normalFont);
        addTableRow(patientTable, "Số CMND:", patient.getIdentityCard(), boldFont, normalFont);
        addTableRow(patientTable, "Số BHYT:", patient.getInsuranceNumber(), boldFont, normalFont);
        addTableRow(patientTable, "Mã bệnh nhân:", patient.getPatientCode(), boldFont, normalFont);
        addTableRow(patientTable, "Người liên hệ khẩn cấp:", patient.getEmergencyContact(), boldFont, normalFont);
        addTableRow(patientTable, "Điện thoại khẩn cấp:", patient.getEmergencyPhone(), boldFont, normalFont);
        
        document.add(patientTable);
        
        // Medical History Section
        Paragraph medicalHistoryHeader = new Paragraph("TIỀN SỬ BỆNH", headerFont);
        medicalHistoryHeader.setSpacingBefore(20);
        document.add(medicalHistoryHeader);
        
        PdfPTable medicalHistoryTable = new PdfPTable(1);
        medicalHistoryTable.setWidthPercentage(100);
        medicalHistoryTable.setSpacingBefore(10);
        medicalHistoryTable.setSpacingAfter(10);
        
        addTableRow(medicalHistoryTable, "Tiền sử bệnh:", patient.getMedicalHistory(), boldFont, normalFont);
        addTableRow(medicalHistoryTable, "Dị ứng:", patient.getAllergies(), boldFont, normalFont);
        
        document.add(medicalHistoryTable);
        
        // Examination Information Section
        Paragraph examHeader = new Paragraph("THÔNG TIN KHÁM BỆNH", headerFont);
        examHeader.setSpacingBefore(20);
        document.add(examHeader);
        
        PdfPTable examTable = new PdfPTable(2);
        examTable.setWidthPercentage(100);
        examTable.setSpacingBefore(10);
        examTable.setSpacingAfter(10);
        
        addTableRow(examTable, "Ngày khám:", formatDateTime(medicalRecord.getExaminationDate()), boldFont, normalFont);
        addTableRow(examTable, "Bác sĩ điều trị:", doctor != null ? doctor.getFullName() : "", boldFont, normalFont);
        addTableRow(examTable, "Lý do đến khám:", medicalRecord.getChiefComplaint(), boldFont, normalFont);
        addTableRow(examTable, "Bệnh sử:", medicalRecord.getHistoryOfPresentIllness(), boldFont, normalFont);
        
        document.add(examTable);
        
        // Clinical Examination Section
        Paragraph clinicalHeader = new Paragraph("KHÁM LÂM SÀNG", headerFont);
        clinicalHeader.setSpacingBefore(20);
        document.add(clinicalHeader);
        
        PdfPTable clinicalTable = new PdfPTable(1);
        clinicalTable.setWidthPercentage(100);
        clinicalTable.setSpacingBefore(10);
        clinicalTable.setSpacingAfter(10);
        
        addTableRow(clinicalTable, "Khám toàn thân:", medicalRecord.getPhysicalExamination(), boldFont, normalFont);
        addTableRow(clinicalTable, "Dấu hiệu sinh tồn:", medicalRecord.getVitalSigns(), boldFont, normalFont);
        
        document.add(clinicalTable);
        
        // Diagnosis Section
        Paragraph diagnosisHeader = new Paragraph("CHẨN ĐOÁN", headerFont);
        diagnosisHeader.setSpacingBefore(20);
        document.add(diagnosisHeader);
        
        PdfPTable diagnosisTable = new PdfPTable(1);
        diagnosisTable.setWidthPercentage(100);
        diagnosisTable.setSpacingBefore(10);
        diagnosisTable.setSpacingAfter(10);
        
        addTableRow(diagnosisTable, "Chẩn đoán:", medicalRecord.getDiagnosis(), boldFont, normalFont);
        addTableRow(diagnosisTable, "Chẩn đoán phân biệt:", medicalRecord.getDifferentialDiagnosis(), boldFont, normalFont);
        
        document.add(diagnosisTable);
        
        // Treatment Section
        Paragraph treatmentHeader = new Paragraph("KẾ HOẠCH ĐIỀU TRỊ", headerFont);
        treatmentHeader.setSpacingBefore(20);
        document.add(treatmentHeader);
        
        PdfPTable treatmentTable = new PdfPTable(1);
        treatmentTable.setWidthPercentage(100);
        treatmentTable.setSpacingBefore(10);
        treatmentTable.setSpacingAfter(10);
        
        addTableRow(treatmentTable, "Phác đồ điều trị:", medicalRecord.getTreatmentPlan(), boldFont, normalFont);
        addTableRow(treatmentTable, "Thuốc:", medicalRecord.getMedications(), boldFont, normalFont);
        addTableRow(treatmentTable, "Hướng dẫn tái khám:", medicalRecord.getFollowUpInstructions(), boldFont, normalFont);
        addTableRow(treatmentTable, "Ghi chú thêm:", medicalRecord.getAdditionalNotes(), boldFont, normalFont);
        
        document.add(treatmentTable);
        
        // Footer Information
        Paragraph footerHeader = new Paragraph("THÔNG TIN THỰC HIỆN", headerFont);
        footerHeader.setSpacingBefore(20);
        document.add(footerHeader);
        
        PdfPTable footerTable = new PdfPTable(2);
        footerTable.setWidthPercentage(100);
        footerTable.setSpacingBefore(10);
        footerTable.setSpacingAfter(10);
        
        addTableRow(footerTable, "Trạng thái:", getStatusDisplay(medicalRecord.getStatus()), boldFont, normalFont);
        addTableRow(footerTable, "Ngày tạo:", formatDateTime(medicalRecord.getCreatedAt()), boldFont, normalFont);
        addTableRow(footerTable, "Mã hồ sơ:", medicalRecord.getRecordId().toString(), boldFont, normalFont);
        
        document.add(footerTable);
        
        // Signature section
        Paragraph signatureHeader = new Paragraph("CHỮ KÝ", headerFont);
        signatureHeader.setSpacingBefore(30);
        document.add(signatureHeader);
        
        PdfPTable signatureTable = new PdfPTable(2);
        signatureTable.setWidthPercentage(100);
        signatureTable.setSpacingBefore(20);
        
        PdfPCell doctorCell = new PdfPCell();
        doctorCell.setBorder(Rectangle.NO_BORDER);
        doctorCell.addElement(new Paragraph("Bác sĩ điều trị", normalFont));
        doctorCell.addElement(Chunk.NEWLINE);
        doctorCell.addElement(Chunk.NEWLINE);
        doctorCell.addElement(Chunk.NEWLINE);
        doctorCell.addElement(new Paragraph(doctor != null ? doctor.getFullName() : "", boldFont));
        
        PdfPCell dateCell = new PdfPCell();
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.addElement(new Paragraph("Ngày " + LocalDate.now().format(DATE_ONLY_FORMATTER), normalFont));
        
        signatureTable.addCell(doctorCell);
        signatureTable.addCell(dateCell);
        
        document.add(signatureTable);
        
        document.close();
        writer.close();
        
        return outputStream.toByteArray();
    }
    
    private static void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "", valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
    
    private static String calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return "";
        }
        return String.valueOf(Period.between(dateOfBirth, LocalDate.now()).getYears());
    }
    
    private static String getGenderDisplay(Patient.Gender gender) {
        if (gender == null) return "";
        switch (gender) {
            case MALE: return "Nam";
            case FEMALE: return "Nữ";
            case OTHER: return "Khác";
            default: return "";
        }
    }
    
    private static String getStatusDisplay(MedicalRecord.Status status) {
        if (status == null) return "";
        switch (status) {
            case DRAFT: return "Bản nháp";
            case COMPLETED: return "Hoàn thành";
            case CANCELLED: return "Đã hủy";
            default: return "";
        }
    }
    
    private static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_ONLY_FORMATTER);
    }
    
    private static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_FORMATTER);
    }
}
