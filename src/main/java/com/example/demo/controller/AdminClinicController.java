package com.example.demo.controller;

import com.example.demo.dto.ClinicRequest;
import com.example.demo.dto.ClinicResponse;
import com.example.demo.service.ClinicService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/clinic")
public class AdminClinicController {
    
    private final ClinicService clinicService;
    private static final String UPLOAD_DIR = "uploads/logo";
    
    public AdminClinicController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }
    
    /**
     * Lấy thông tin phòng khám
     */
    @GetMapping
    public ResponseEntity<ClinicResponse> getClinicInfo() {
        ClinicResponse response = clinicService.getClinicInfo();
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cập nhật thông tin phòng khám
     */
    @PutMapping
    public ResponseEntity<ClinicResponse> updateClinicInfo(@Valid @RequestBody ClinicRequest request) {
        return ResponseEntity.ok(clinicService.updateClinicInfo(request));
    }
    
    /**
     * Upload logo phòng khám
     */
    @PostMapping("/upload-logo")
    public ResponseEntity<?> uploadLogo(@RequestParam("logo") MultipartFile logo) {
        if (logo.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "File logo không được để trống"));
        }
        
        // Kiểm tra định dạng file
        String contentType = logo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "File phải là hình ảnh"));
        }
        
        // Kiểm tra xem đã có thông tin phòng khám chưa
        ClinicResponse existingClinic = clinicService.getClinicInfo();
        if (existingClinic == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Vui lòng tạo thông tin phòng khám trước khi upload logo"));
        }
        
        try {
            // Tạo thư mục nếu chưa tồn tại
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Xóa logo cũ nếu có
            if (existingClinic.getLogoUrl() != null && existingClinic.getLogoUrl().startsWith("/uploads/logo/")) {
                try {
                    String oldFileName = existingClinic.getLogoUrl().substring("/uploads/logo/".length());
                    Path oldFilePath = uploadPath.resolve(oldFileName);
                    if (Files.exists(oldFilePath)) {
                        Files.delete(oldFilePath);
                    }
                } catch (IOException e) {
                    // Log warning nhưng không fail upload mới
                    System.err.println("Không thể xóa logo cũ: " + e.getMessage());
                }
            }
            
            // Tạo tên file unique
            String originalFilename = logo.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = "clinic_logo_" + UUID.randomUUID().toString() + extension;
            
            // Lưu file
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(logo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Tạo URL để lưu vào database
            String logoUrl = "/uploads/logo/" + fileName;
            
            // Cập nhật logoUrl vào database (chỉ update logoUrl, giữ nguyên các field khác)
            ClinicRequest updateRequest = new ClinicRequest();
            updateRequest.setName(existingClinic.getName()); // Giữ nguyên name để pass validation
            updateRequest.setLogoUrl(logoUrl);
            ClinicResponse response = clinicService.updateClinicInfo(updateRequest);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Upload logo thành công");
            result.put("logoUrl", logoUrl);
            result.put("clinicInfo", response);
            
            return ResponseEntity.ok(result);
            
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi upload file: " + e.getMessage()));
        }
    }
}

