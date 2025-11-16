// com/example/demo/service/DoctorPatientService.java
package com.example.demo.service;

import com.example.demo.entity.Patient;
import com.example.demo.repository.DoctorPatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorPatientService {

    @Autowired
    private DoctorPatientRepository doctorPatientRepository;

    public Page<Patient> getMyPatients(String doctorId, int page, int size, String sortBy) {
        Sort sort = Sort.by(sortBy != null ? sortBy : "fullName").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return doctorPatientRepository.findPatientsByDoctorId(doctorId, pageable);
    }

    public List<Patient> searchMyPatients(String doctorId, String keyword) {
        // Tìm trong danh sách bệnh nhân của bác sĩ
        return doctorPatientRepository.findPatientsByDoctorId(doctorId).stream()
                .filter(p -> p.getFullName().toLowerCase().contains(keyword.toLowerCase()) ||
                        p.getPhone().contains(keyword))
                .sorted((a, b) -> a.getFullName().compareToIgnoreCase(b.getFullName()))
                .toList();
    }
}