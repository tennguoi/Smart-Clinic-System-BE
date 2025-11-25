//package com.example.demo.config;
//
//import java.time.LocalDate;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import com.example.demo.entity.Role;
//import com.example.demo.entity.User;
//import com.example.demo.entity.UserRole;
//import com.example.demo.repository.PermissionRepository;
//import com.example.demo.repository.RolePermissionRepository;
//import com.example.demo.repository.RoleRepository;
//import com.example.demo.repository.UserRepository;
//import com.example.demo.repository.UserRoleRepository;
//
//@Component
//public class DataInitializer implements CommandLineRunner {
//
//   @Autowired
//   private UserRepository userRepository;
//
//   @Autowired
//   private RoleRepository roleRepository;
//
//   @Autowired
//   private UserRoleRepository userRoleRepository;
//
//   @Autowired
//   private PermissionRepository permissionRepository;
//
//   @Autowired
//   private RolePermissionRepository rolePermissionRepository;
//
//   @Autowired
//   private PasswordEncoder passwordEncoder;
//
//   @Override
//   public void run(String... args) throws Exception {
//       // Clear existing data (optional - only for development)
//       // userRoleRepository.deleteAll();
//       // rolePermissionRepository.deleteAll();
//       // userRepository.deleteAll();
//       // roleRepository.deleteAll();
//       // permissionRepository.deleteAll();
//
//       // Create Roles
//       Role adminRole = createRoleIfNotExists("admin", "Administrator role with full system access");
//       Role doctorRole = createRoleIfNotExists("bac_si", "Doctor role for medical professionals");
//       Role receptionistRole = createRoleIfNotExists("tiep_tan", "Receptionist role for front desk staff");
//
//       // Create sample users if they don't exist
//       if (!userRepository.existsByEmail("admin@clinic.com")) {
//           User admin = User.builder()
//                   .email("admin@clinic.com")
//                   .password(passwordEncoder.encode("admin123"))
//                   .fullName("Nguyễn Văn Admin")
//                   .phone("0123456789")
//                   .dob(LocalDate.of(1985, 5, 15))
//                   .gender(User.Gender.male)
//                   .address("123 Đường ABC, Quận 1, TP.HCM")
//                   .photoUrl("https://example.com/photos/admin.jpg")
//                   .bio("System Administrator")
//                   .isVerified(true)
//                   .twoFactorEnabled(false)
//                   .build();
//           admin = userRepository.save(admin);
//
//           UserRole adminUserRole = UserRole.builder()
//                   .user(admin)
//                   .role(adminRole)
//                   .build();
//           userRoleRepository.save(adminUserRole);
//           System.out.println("Created admin user: admin@clinic.com / admin123");
//       }
//
//       if (!userRepository.existsByEmail("d")) {
//           User doctor = User.builder()
//                   .email("congminh152005@gmail.com")
//                   .password(passwordEncoder.encode("doctor123"))
//                   .fullName("Phạm Minh Đàn")
//                   .phone("0987654321")
//                   .dob(LocalDate.of(1990, 8, 20))
//                   .gender(User.Gender.female)
//                   .address("456 Đường XYZ, Quận 3, TP.HCM")
//                   .experienceYears(5)
//                   .photoUrl("https://tse3.mm.bing.net/th/id/OIP.HXWtzVpE_QndSbr-NaRPRAHaIp?cb=ucfimgc2&rs=1&pid=ImgDetMain&o=7&rm=3")
//                   .bio("Bác sĩ chuyên khoa nội tổng quát ")
//                   .isVerified(true)
//                   .twoFactorEnabled(false)
//                   .build();
//           doctor = userRepository.save(doctor);
//
//           UserRole doctorUserRole = UserRole.builder()
//                   .user(doctor)
//                   .role(doctorRole)
//                   .build();
//           userRoleRepository.save(doctorUserRole);
//           System.out.println("Created doctor user: doctor@clinic.com / doctor123");
//       }
//
//       if (!userRepository.existsByEmail("receptionist@clinic.com")) {
//           User receptionist = User.builder()
//                   .email("danpham261005@gmail.com")
//                   .password(passwordEncoder.encode("receptionist123"))
//                   .fullName("Nguyễn Danh Hiếu")
//                   .phone("0912345678")
//                   .dob(LocalDate.of(1995, 3, 10))
//                   .gender(User.Gender.male)
//                   .address("789 Đường DEF, Quận 7, TP.HCM")
//                   .photoUrl("https://example.com/photos/receptionist.jpg")
//                   .bio("Nhân viên tiếp tân phòng khám")
//                   .isVerified(true)
//                   .twoFactorEnabled(false)
//                   .build();
//           receptionist = userRepository.save(receptionist);
//
//           UserRole receptionistUserRole = UserRole.builder()
//                   .user(receptionist)
//                   .role(receptionistRole)
//                   .build();
//           userRoleRepository.save(receptionistUserRole);
//           System.out.println("Created receptionist user: receptionist@clinic.com / receptionist123");
//       }
//
//       System.out.println("Data initialization completed!");
//   }
//
//   private Role createRoleIfNotExists(String name, String description) {
//       return roleRepository.findByName(name)
//               .orElseGet(() -> {
//                   Role role = Role.builder()
//                           .name(name)
//                           .description(description)
//                           .build();
//                   return roleRepository.save(role);
//               });
//   }
//}
//
