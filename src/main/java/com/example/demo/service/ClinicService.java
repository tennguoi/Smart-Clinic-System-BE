package com.example.demo.service;

import com.example.demo.dto.ClinicRequest;
import com.example.demo.dto.ClinicResponse;
import com.example.demo.entity.Clinic;
import com.example.demo.repository.ClinicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ClinicService {
    private final ClinicRepository clinicRepository;
    
    public ClinicService(ClinicRepository clinicRepository) {
        this.clinicRepository = clinicRepository;
    }
    
    /**
     * Lấy thông tin phòng khám
     * Trả về null nếu chưa có dữ liệu
     */
    public ClinicResponse getClinicInfo() {
        Optional<Clinic> clinicOpt = clinicRepository.findFirstByOrderByCreatedAtAsc();
        
        if (clinicOpt.isPresent()) {
            return mapToResponse(clinicOpt.get());
        } else {
            return null; // Không tự động tạo dữ liệu mặc định
        }
    }
    
    /**
     * Cập nhật thông tin phòng khám
     */
    @Transactional
    public ClinicResponse updateClinicInfo(ClinicRequest request) {
        Optional<Clinic> clinicOpt = clinicRepository.findFirstByOrderByCreatedAtAsc();
        
        Clinic clinic;
        if (clinicOpt.isPresent()) {
            clinic = clinicOpt.get();
        } else {
            clinic = new Clinic();
        }
        
        // Cập nhật thông tin
        if (request.getName() != null) {
            clinic.setName(request.getName());
        }
        if (request.getAddress() != null) {
            clinic.setAddress(request.getAddress());
        }
        if (request.getPhone() != null) {
            clinic.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            clinic.setEmail(request.getEmail());
        }
        if (request.getWebsite() != null) {
            clinic.setWebsite(request.getWebsite());
        }
        if (request.getLogoUrl() != null) {
            clinic.setLogoUrl(request.getLogoUrl());
        }
        
        Clinic saved = clinicRepository.save(clinic);
        return mapToResponse(saved);
    }
    
    /**
     * Lấy entity Clinic (dùng cho PdfExportService)
     * Trả về null nếu chưa có dữ liệu
     */
    public Clinic getClinicEntity() {
        Optional<Clinic> clinicOpt = clinicRepository.findFirstByOrderByCreatedAtAsc();
        return clinicOpt.orElse(null); // Không tự động tạo dữ liệu mặc định
    }
    
    private ClinicResponse mapToResponse(Clinic clinic) {
        return ClinicResponse.builder()
                .clinicId(clinic.getClinicId())
                .name(clinic.getName())
                .address(clinic.getAddress())
                .phone(clinic.getPhone())
                .email(clinic.getEmail())
                .website(clinic.getWebsite())
                .logoUrl(clinic.getLogoUrl())
                .createdAt(clinic.getCreatedAt())
                .updatedAt(clinic.getUpdatedAt())
                .build();
    }
}

