package com.example.demo.config;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Role adminRole = createRoleIfNotExists("admin", "Administrator role with full system access");
        Role doctorRole = createRoleIfNotExists("bac_si", "Doctor role for medical professionals");
        Role receptionistRole = createRoleIfNotExists("tiep_tan", "Receptionist role for front desk staff");

        if (!userRepository.existsByEmail("admin@clinic.com")) {
            User admin = User.builder()
                    .email("admin@clinic.com")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Nguyễn Văn Admin")
                    .phone("0123456789")
                    .dob(LocalDate.of(1985, 5, 15))
                    .gender(User.Gender.male)
                    .address("123 Đường ABC, Quận 1, TP.HCM")
                    .photoUrl("https://example.com/photos/admin.jpg")
                    .bio("System Administrator")
                    .isActive(true)
                    .twoFactorEnabled(false)
                    .otpCode(null)
                    .otpExpiresAt(null)
                    .build();
            admin = userRepository.save(admin);

            UserRole adminUserRole = UserRole.builder()
                    .user(admin)
                    .role(adminRole)
                    .build();
            userRoleRepository.save(adminUserRole);
            System.out.println("Created admin user: admin@clinic.com / admin123");
        }

        if (!userRepository.existsByEmail("phamdanngu937@gmail.com")) {
            User doctor = User.builder()
                    .email("phamdanngu937@gmail.com")
                    .password(passwordEncoder.encode("doctor123"))
                    .fullName("Phạm Minh Đàn")
                    .phone("0987654321")
                    .dob(LocalDate.of(1990, 8, 20))
                    .gender(User.Gender.female)
                    .address("456 Đường XYZ, Quận 3, TP.HCM")
                    .experienceYears(5)
                    .photoUrl("https://example.com/photos/doctor.jpg")
                    .bio("Bác sĩ chuyên khoa nội tổng quát với 5 năm kinh nghiệm")
                    .isActive(true)
                    .twoFactorEnabled(false)
                    .otpCode(null)
                    .otpExpiresAt(null)
                    .build();
            doctor = userRepository.save(doctor);

            UserRole doctorUserRole = UserRole.builder()
                    .user(doctor)
                    .role(doctorRole)
                    .build();
            userRoleRepository.save(doctorUserRole);
            System.out.println("Created doctor user: doctor@clinic.com / doctor123");
        }

        if (!userRepository.existsByEmail("receptionist@clinic.com")) {
            User receptionist = User.builder()
                    .email("danpham261005@gmail.com")
                    .password(passwordEncoder.encode("receptionist123"))
                    .fullName("Nguyễn Danh Hiếu")
                    .phone("0912345678")
                    .dob(LocalDate.of(1995, 3, 10))
                    .gender(User.Gender.male)
                    .address("789 Đường DEF, Quận 7, TP.HCM")
                    .photoUrl("https://example.com/photos/receptionist.jpg")
                    .bio("Nhân viên tiếp tân phòng khám")
                    .isActive(true)
                    .twoFactorEnabled(false)
                    .otpCode(null)
                    .otpExpiresAt(null)
                    .build();
            receptionist = userRepository.save(receptionist);

            UserRole receptionistUserRole = UserRole.builder()
                    .user(receptionist)
                    .role(receptionistRole)
                    .build();
            userRoleRepository.save(receptionistUserRole);
            System.out.println("Created receptionist user: receptionist@clinic.com / receptionist123");
        }

        if (!userRepository.existsByEmail("doctor1@clinic.com")) {
            User doctor1 = User.builder()
                    .email("doctor1@clinic.com")
                    .password(passwordEncoder.encode("doctor123"))
                    .fullName("Bác sĩ Nguyễn Văn A")
                    .phone("0123456780")
                    .dob(LocalDate.of(1988, 6, 15))
                    .gender(User.Gender.male)
                    .address("123 Đường Nguyễn Huệ, Quận 1, TP.HCM")
                    .experienceYears(10)
                    .photoUrl("https://example.com/photos/doctor1.jpg")
                    .bio("Bác sĩ chuyên khoa tim mạch với 10 năm kinh nghiệm. Tốt nghiệp Đại học Y Dược TP.HCM.")
                    .isActive(true)
                    .twoFactorEnabled(false)
                    .otpCode(null)
                    .otpExpiresAt(null)
                    .build();
            doctor1 = userRepository.save(doctor1);

            UserRole doctor1UserRole = UserRole.builder()
                    .user(doctor1)
                    .role(doctorRole)
                    .build();
            userRoleRepository.save(doctor1UserRole);
            System.out.println("Created doctor1 user: doctor1@clinic.com / doctor123");
        }

        if (!userRepository.existsByEmail("doctor2@clinic.com")) {
            User doctor2 = User.builder()
                    .email("doctor2@clinic.com")
                    .password(passwordEncoder.encode("doctor123"))
                    .fullName("Bác sĩ Trần Thị B")
                    .phone("0987654320")
                    .dob(LocalDate.of(1992, 3, 22))
                    .gender(User.Gender.female)
                    .address("456 Đường Lê Lợi, Quận 3, TP.HCM")
                    .experienceYears(7)
                    .photoUrl("https://example.com/photos/doctor2.jpg")
                    .bio("Bác sĩ chuyên khoa nhi với 7 năm kinh nghiệm. Chuyên điều trị các bệnh về trẻ em.")
                    .isActive(true)
                    .twoFactorEnabled(false)
                    .otpCode(null)
                    .otpExpiresAt(null)
                    .build();
            doctor2 = userRepository.save(doctor2);

            UserRole doctor2UserRole = UserRole.builder()
                    .user(doctor2)
                    .role(doctorRole)
                    .build();
            userRoleRepository.save(doctor2UserRole);
            System.out.println("Created doctor2 user: doctor2@clinic.com / doctor123");
        }

        if (!userRepository.existsByEmail("doctor3@clinic.com")) {
            User doctor3 = User.builder()
                    .email("doctor3@clinic.com")
                    .password(passwordEncoder.encode("doctor123"))
                    .fullName("Bác sĩ Lê Văn C")
                    .phone("0911223344")
                    .dob(LocalDate.of(1985, 11, 8))
                    .gender(User.Gender.male)
                    .address("789 Đường Võ Văn Tần, Quận 10, TP.HCM")
                    .experienceYears(15)
                    .photoUrl("https://example.com/photos/doctor3.jpg")
                    .bio("Bác sĩ chuyên khoa ngoại tổng quát với 15 năm kinh nghiệm. Chuyên phẫu thuật và điều trị các bệnh ngoại khoa.")
                    .isActive(true)
                    .twoFactorEnabled(false)
                    .otpCode(null)
                    .otpExpiresAt(null)
                    .build();
            doctor3 = userRepository.save(doctor3);

            UserRole doctor3UserRole = UserRole.builder()
                    .user(doctor3)
                    .role(doctorRole)
                    .build();
            userRoleRepository.save(doctor3UserRole);
            System.out.println("Created doctor3 user: doctor3@clinic.com / doctor123");
        }

        System.out.println("Data initialization completed!");
    }

    private Role createRoleIfNotExists(String name, String description) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role role = Role.builder()
                            .name(name)
                            .description(description)
                            .build();
                    return roleRepository.save(role);
                });
    }
}
