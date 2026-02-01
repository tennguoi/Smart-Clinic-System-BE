package com.example.demo.service;

import com.example.demo.dto.doctor.DoctorPublicResponse;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
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
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    /**
     * Lấy danh sách tất cả bác sĩ (users có ROLE_DOCTOR)
     * Chỉ trả về thông tin công khai, không bao gồm password và thông tin nhạy cảm
     */
    public List<DoctorPublicResponse> getAllDoctors() {
        List<User> allUsers = userRepository.findAll();
        
        return allUsers.stream()
                .filter(user -> {
                    // Lọc users có role ROLE_DOCTOR
                    List<String> roleNames = userRoleRepository.findByUser_UserId(user.getUserId())
                            .stream()
                            .map(ur -> ur.getRole().getName())
                            .collect(Collectors.toList());
                    return roleNames.contains("ROLE_DOCTOR") || roleNames.contains("role_doctor");
                })
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
