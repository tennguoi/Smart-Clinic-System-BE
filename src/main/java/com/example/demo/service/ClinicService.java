package com.example.demo.service;

import com.example.demo.dto.ClinicRequest;
import com.example.demo.dto.ClinicResponse;
import com.example.demo.entity.Clinic;
import com.example.demo.repository.ClinicRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ClinicService {
    private static final Logger logger = LoggerFactory.getLogger(ClinicService.class);
    private static final String EMAIL_REGEX = 
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final String LOGO_URL_REGEX = 
        "^/uploads/logo/[a-zA-Z0-9_-]+\\.(jpg|jpeg|png|gif|webp)$";
    
    private final ClinicRepository clinicRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public ClinicService(ClinicRepository clinicRepository) {
        this.clinicRepository = clinicRepository;
    }
    
    /**
     * Lấy thông tin phòng khám
     * Trả về null nếu chưa có dữ liệu
     */
    @Transactional(readOnly = true)
    public ClinicResponse getClinicInfo() {
        logger.debug("Đang lấy thông tin phòng khám từ database");
        
        // Clear cache để đảm bảo lấy dữ liệu mới nhất
        entityManager.clear();
        
        Optional<Clinic> clinicOpt = clinicRepository.findFirstByOrderByCreatedAtAsc();
        
        if (clinicOpt.isPresent()) {
            Clinic clinic = clinicOpt.get();
            logger.debug("Đã tìm thấy phòng khám với ID: {}, name: {}", clinic.getClinicId(), clinic.getName());
            return mapToResponse(clinic);
        } else {
            logger.debug("Không tìm thấy thông tin phòng khám trong database");
            return null; // Không tự động tạo dữ liệu mặc định
        }
    }
    
    /**
     * Cập nhật thông tin phòng khám
     */
    @Transactional
    public ClinicResponse updateClinicInfo(ClinicRequest request) {
        logger.info("Bắt đầu cập nhật thông tin phòng khám với request: {}", request);
        
        Optional<Clinic> clinicOpt = clinicRepository.findFirstByOrderByCreatedAtAsc();
        
        Clinic clinic;
        boolean isNew = false;
        if (clinicOpt.isPresent()) {
            clinic = clinicOpt.get();
            logger.info("Tìm thấy phòng khám hiện tại với ID: {}", clinic.getClinicId());
        } else {
            clinic = new Clinic();
            isNew = true;
            logger.info("Không tìm thấy phòng khám, tạo mới");
        }
        
        // Validation: Nếu là tạo mới, name là bắt buộc
        if (isNew && (request.getName() == null || request.getName().trim().isEmpty())) {
            logger.error("Lỗi: Tên phòng khám không được để trống khi tạo mới");
            throw new IllegalArgumentException("Tên phòng khám không được để trống");
        }
        
        // Cập nhật thông tin (chỉ update các field không null)
        // Name: bắt buộc không được rỗng khi update
        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Tên phòng khám không được để trống");
            }
            clinic.setName(name);
        }
        
        // Address: cho phép rỗng (set null nếu empty)
        if (request.getAddress() != null) {
            String address = request.getAddress().trim();
            clinic.setAddress(address.isEmpty() ? null : address);
        }
        
        // Phone: cho phép rỗng (set null nếu empty)
        if (request.getPhone() != null) {
            String phone = request.getPhone().trim();
            clinic.setPhone(phone.isEmpty() ? null : phone);
        }
        
        // Email: validate nếu không rỗng, set null nếu empty
        if (request.getEmail() != null) {
            String email = request.getEmail().trim();
            if (!email.isEmpty() && !email.matches(EMAIL_REGEX)) {
                throw new IllegalArgumentException("Email không hợp lệ");
            }
            clinic.setEmail(email.isEmpty() ? null : email);
        }
        
        // Website: cho phép rỗng (set null nếu empty)
        if (request.getWebsite() != null) {
            String website = request.getWebsite().trim();
            clinic.setWebsite(website.isEmpty() ? null : website);
        }
        
        // LogoUrl: validate format và cho phép rỗng (set null nếu empty)
        if (request.getLogoUrl() != null) {
            String logoUrl = request.getLogoUrl().trim();
            if (!logoUrl.isEmpty() && !logoUrl.matches(LOGO_URL_REGEX)) {
                throw new IllegalArgumentException("Logo URL không hợp lệ. URL phải có định dạng: /uploads/logo/filename.ext");
            }
            clinic.setLogoUrl(logoUrl.isEmpty() ? null : logoUrl);
        }
        
        // Thời gian làm việc buổi sáng
        if (request.getMorningStartTime() != null) {
            clinic.setMorningStartTime(request.getMorningStartTime());
        }
        
        if (request.getMorningEndTime() != null) {
            clinic.setMorningEndTime(request.getMorningEndTime());
        }
        
        // Thời gian làm việc buổi chiều
        if (request.getAfternoonStartTime() != null) {
            clinic.setAfternoonStartTime(request.getAfternoonStartTime());
        }
        
        if (request.getAfternoonEndTime() != null) {
            clinic.setAfternoonEndTime(request.getAfternoonEndTime());
        }
        
        Clinic saved = clinicRepository.save(clinic);
        
        // Force flush và refresh để đảm bảo dữ liệu được lưu vào database và đọc lại
        entityManager.flush();
        entityManager.refresh(saved);
        
        logger.info("Đã lưu và refresh thành công thông tin phòng khám với ID: {}", saved.getClinicId());
        logger.info("Thông tin chi tiết sau khi lưu: name={}, address={}, phone={}, email={}, website={}, logoUrl={}", 
                    saved.getName(), saved.getAddress(), saved.getPhone(), saved.getEmail(), saved.getWebsite(), saved.getLogoUrl());
        
        ClinicResponse response = mapToResponse(saved);
        logger.info("Response trả về: {}", response);
        return response;
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
                .morningStartTime(clinic.getMorningStartTime())
                .morningEndTime(clinic.getMorningEndTime())
                .afternoonStartTime(clinic.getAfternoonStartTime())
                .afternoonEndTime(clinic.getAfternoonEndTime())
                .createdAt(clinic.getCreatedAt())
                .updatedAt(clinic.getUpdatedAt())
                .build();
    }
}

