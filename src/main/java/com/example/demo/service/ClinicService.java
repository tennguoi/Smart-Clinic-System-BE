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
        boolean isNew = false;
        if (clinicOpt.isPresent()) {
            clinic = clinicOpt.get();
        } else {
            clinic = new Clinic();
            isNew = true;
        }
        
        // Validation: Nếu là tạo mới, name là bắt buộc
        if (isNew && (request.getName() == null || request.getName().trim().isEmpty())) {
            throw new IllegalArgumentException("Tên phòng khám không được để trống");
        }
        
        // Cập nhật thông tin (chỉ update các field không null)
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            clinic.setName(request.getName().trim());
        }
        if (request.getAddress() != null) {
            clinic.setAddress(request.getAddress().trim());
        }
        if (request.getPhone() != null) {
            clinic.setPhone(request.getPhone().trim());
        }
        if (request.getEmail() != null) {
            String email = request.getEmail().trim();
            if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new IllegalArgumentException("Email không hợp lệ");
            }
            clinic.setEmail(email.isEmpty() ? null : email);
        }
        if (request.getWebsite() != null) {
            clinic.setWebsite(request.getWebsite().trim());
        }
        if (request.getLogoUrl() != null) {
            clinic.setLogoUrl(request.getLogoUrl().trim());
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

