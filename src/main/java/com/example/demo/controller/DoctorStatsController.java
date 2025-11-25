package com.example.demo.controller;

import com.example.demo.dto.doctor.DoctorStatsDetailResponse;
import com.example.demo.dto.doctor.DoctorStatsResponse;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserRoleRepository;
import com.example.demo.enums.DoctorStatsRange;
import com.example.demo.service.DoctorStatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctors")
public class DoctorStatsController {

    private final DoctorStatsService doctorStatsService;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public DoctorStatsController(DoctorStatsService doctorStatsService,
                                 UserRepository userRepository,
                                 UserRoleRepository userRoleRepository) {
        this.doctorStatsService = doctorStatsService;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @GetMapping("/{doctorId}/stats")
    public ResponseEntity<DoctorStatsResponse> getDoctorStats(
            @PathVariable UUID doctorId,
            @RequestParam(defaultValue = "day") String range,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        ensureDoctorAccess(doctorId);

        DoctorStatsRange statsRange = parseRange(range);
        DoctorStatsResponse response = doctorStatsService.getDoctorStats(doctorId, statsRange, date);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{doctorId}/stats/detail")
    public ResponseEntity<DoctorStatsDetailResponse> getDoctorStatsDetail(
            @PathVariable UUID doctorId,
            @RequestParam(defaultValue = "day") String range,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        ensureDoctorAccess(doctorId);
        DoctorStatsRange statsRange = parseRange(range);

        DoctorStatsDetailResponse response = doctorStatsService.getDoctorStatsDetail(
                doctorId,
                statsRange,
                date,
                page,
                pageSize
        );

        return ResponseEntity.ok(response);
    }

    private void ensureDoctorAccess(UUID doctorId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bạn cần đăng nhập để xem thống kê");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không tìm thấy người dùng"));

        if (!doctorId.equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không được phép xem thống kê của bác sĩ khác");
        }

        // Kiểm tra user có role bác sĩ hoặc admin
        boolean isDoctorOrAdmin = userRoleRepository.findByUser_UserId(user.getUserId()).stream()
                .anyMatch(ur -> {
                    String roleName = ur.getRole().getName();
                    return roleName != null && (
                            roleName.equalsIgnoreCase("ROLE_BAC_SI") ||
                            roleName.equalsIgnoreCase("bac_si") ||
                            roleName.equalsIgnoreCase("ROLE_ADMIN") ||
                            roleName.equalsIgnoreCase("admin")
                    );
                });

        if (!isDoctorOrAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ bác sĩ mới được xem thống kê");
        }
    }

    private DoctorStatsRange parseRange(String range) {
        try {
            return DoctorStatsRange.from(range);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }
}

