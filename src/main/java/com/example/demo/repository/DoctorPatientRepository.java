// com/example/demo/repository/DoctorPatientRepository.java
package com.example.demo.repository;

import com.example.demo.entity.DoctorPatient;
import com.example.demo.entity.DoctorPatientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorPatientRepository extends JpaRepository<DoctorPatient, DoctorPatientId> {

    // Lấy danh sách bệnh nhân của bác sĩ hiện tại
    @Query("SELECT dp.patient FROM DoctorPatient dp WHERE dp.doctorId = :doctorId")
    List<com.example.demo.entity.Patient> findPatientsByDoctorId(String doctorId);

    // Phân trang
    @Query("SELECT dp.patient FROM DoctorPatient dp WHERE dp.doctorId = :doctorId")
    org.springframework.data.domain.Page<com.example.demo.entity.Patient> findPatientsByDoctorId(
            String doctorId, org.springframework.data.domain.Pageable pageable);
}