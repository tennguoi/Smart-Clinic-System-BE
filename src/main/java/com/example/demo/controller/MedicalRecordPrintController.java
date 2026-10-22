package com.example.demo.controller;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.dto.MedicalRecordDetailResponse;
import com.example.demo.dto.prescription.PrescriptionResponse;
import com.example.demo.service.MedicalRecordService;

@Controller
@RequestMapping("/api/doctor/medical-records")
@PreAuthorize("hasAnyRole('BAC_SI', 'ADMIN')")
public class MedicalRecordPrintController {
    private final MedicalRecordService medicalRecordService;

    public MedicalRecordPrintController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @GetMapping("/{recordId}/print")
    public String printRecord(@PathVariable UUID recordId, Model model) {
        MedicalRecordDetailResponse detail = medicalRecordService.getRecordDetail(recordId);
        model.addAttribute("record", detail);
        PrescriptionResponse pres = detail.getPrescription();
        model.addAttribute("prescription", pres);
        return "medical-record-print";
    }
}
