package com.example.demo.controller.common;

import com.example.demo.dto.ClinicResponse;
import com.example.demo.service.ClinicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/clinic")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174",
        "http://localhost:3000",
        "http://127.0.0.1:5173"
})
public class PublicClinicController {
    
    private final ClinicService clinicService;
    
    public PublicClinicController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }
    
    /**
     * Lấy thông tin phòng khám (public - không cần authentication)
     * Dùng cho trang chủ của khách hàng
     */
    @GetMapping
    public ResponseEntity<ClinicResponse> getClinicInfo() {
        ClinicResponse response = clinicService.getClinicInfo();
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        // Thêm cache headers để đảm bảo luôn lấy dữ liệu mới nhất từ database
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(response);
    }
}

