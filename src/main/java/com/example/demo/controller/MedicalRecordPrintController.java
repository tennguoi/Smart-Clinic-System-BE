package com.example.demo.controller;

import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.dto.MedicalRecordDetailResponse;
import com.example.demo.dto.PrescriptionResponse;
import com.example.demo.service.MedicalRecordService;
import com.example.demo.service.ThymeleafPdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/api/doctor/medical-records")
public class MedicalRecordPrintController {
    private final MedicalRecordService medicalRecordService;
    private final ThymeleafPdfService thymeleafPdfService;

    public MedicalRecordPrintController(MedicalRecordService medicalRecordService, ThymeleafPdfService thymeleafPdfService) {
        this.medicalRecordService = medicalRecordService;
        this.thymeleafPdfService = thymeleafPdfService;
    }

    @GetMapping("/{recordId}/print")
    public String printRecord(@PathVariable UUID recordId, Model model) {
        MedicalRecordDetailResponse detail = medicalRecordService.getRecordDetail(recordId);
        model.addAttribute("record", detail);
        PrescriptionResponse pres = detail.getPrescription();
        model.addAttribute("prescription", pres);
        return "medical-record-print";
    }

    @GetMapping("/{recordId}/export-pdf-html")
    public ResponseEntity<byte[]> exportPdfHtml(@PathVariable UUID recordId) {
        byte[] pdfBytes = thymeleafPdfService.generatePdfForRecord(recordId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=medical-record-" + recordId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
