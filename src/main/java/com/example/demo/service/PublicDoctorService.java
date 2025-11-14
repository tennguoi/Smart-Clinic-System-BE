package com.example.demo.service;

import com.example.demo.dto.DoctorPublicResponse;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PublicDoctorService {

    @Autowired
    private UserRoleRepository userRoleRepository;

    /**
     * Lấy danh sách tất cả bác sĩ (users có ROLE_DOCTOR)
     * Chỉ trả về thông tin công khai, không bao gồm password và thông tin nhạy cảm
     */
    public List<DoctorPublicResponse> getAllDoctors() {
        // Tìm các userRole có role bác sĩ
        List<UserRole> doctorUserRoles = userRoleRepository.findByRole_NameIgnoreCase("ROLE_BAC_SI");

        return doctorUserRoles.stream()
                .map(UserRole::getUser)
                .distinct()
                .map(this::mapToDoctorPublicResponse)
                .collect(Collectors.toList());
    }

    private DoctorPublicResponse mapToDoctorPublicResponse(User user) {
        return DoctorPublicResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .photoUrl(user.getPhotoUrl())
                .bio(user.getBio())
                .experienceYears(user.getExperienceYears())
                .build();
    }
}
