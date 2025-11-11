package com.example.demo.service;

import com.example.demo.dto.DoctorResponse;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class DoctorService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;


    public List<DoctorResponse> getAllDoctors() {
        // Lấy tất cả UserRole có role là "bac_si"
        List<UserRole> doctorUserRoles = userRoleRepository.findByRoleName("bac_si");
        
        System.out.println("Found " + doctorUserRoles.size() + " doctor user roles");

        // Lấy danh sách users và filter chỉ những user đang active
        List<DoctorResponse> doctors = doctorUserRoles.stream()
                .map(UserRole::getUser)
                .filter(user -> {
                    boolean isActive = user.getIsActive() != null && user.getIsActive();
                    if (!isActive) {
                        System.out.println("Filtered out inactive user: " + user.getEmail());
                    }
                    return isActive;
                })
                .map(this::buildDoctorResponse)
                .collect(Collectors.toList());
        
        System.out.println("Returning " + doctors.size() + " active doctors");
        return doctors;
    }


    public DoctorResponse getDoctorById(UUID doctorId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Bác sĩ không tồn tại"));

        // Kiểm tra user có phải là bác sĩ không
        boolean isDoctor = userRoleRepository.findByUser_UserId(doctorId)
                .stream()
                .anyMatch(ur -> ur.getRole().getName().equals("bac_si"));

        if (!isDoctor) {
            throw new RuntimeException("Người dùng này không phải là bác sĩ");
        }

        if (doctor.getIsActive() == null || !doctor.getIsActive()) {
            throw new RuntimeException("Bác sĩ không còn hoạt động");
        }

        return buildDoctorResponse(doctor);
    }


    private DoctorResponse buildDoctorResponse(User user) {
        return DoctorResponse.builder()

                .fullName(user.getFullName())
                .photoUrl(user.getPhotoUrl())
                .bio(user.getBio())
                .experienceYears(user.getExperienceYears())

                .build();
    }
}

