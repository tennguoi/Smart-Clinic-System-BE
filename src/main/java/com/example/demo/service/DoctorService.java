package com.example.demo.service;

import com.example.demo.dto.DoctorResponse;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DoctorService {

    @Autowired
    private UserRoleRepository userRoleRepository;

    public List<DoctorResponse> getAllDoctors() {
        List<UserRole> doctorUserRoles = userRoleRepository.findByRoleName("bac_si");
        System.out.println("Found " + doctorUserRoles.size() + " doctor user roles");

        List<DoctorResponse> doctors = doctorUserRoles.stream()
                .map(UserRole::getUser)
                .filter(user -> {
                    boolean isActive = user.getIsActive() != null && user.getIsActive();
                    if (!isActive) {
                        System.out.println("Filtered out inactive user: " + user.getEmail());
                    }
                    return isActive;
                })
                .map(this::buildDoctorListItemResponse)
                .collect(Collectors.toList());

        System.out.println("Returning " + doctors.size() + " active doctors");
        return doctors;
    }

    private DoctorResponse buildDoctorListItemResponse(User user) {
        return DoctorResponse.builder()
                .fullName(user.getFullName())
                .photoUrl(user.getPhotoUrl())
                .bio(user.getBio())
                .experienceYears(user.getExperienceYears())
                .build();
    }
}
