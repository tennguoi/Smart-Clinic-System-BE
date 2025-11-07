package com.example.demo.service;
import com.example.demo.dto.AppointmentRespone;
import com.example.demo.entity.Appointment;
import com.example.demo.repository.AppointmentRepository;
import org.springframework.transaction.annotation.Transactional;

public class AppointmentService {
    @Transactional
    public AppointmentResponse quickBook(AppointmentRequest req) {
        Appointment entity = Appointment.builder()
                .patientName(req.patientName())
                .phone(req.phone())
                .email(req.email())
                .appointmentTime(req.appointmentTime())
                .notes(req.notes())
                .quickBook(true)
                .appointmentCode(generateUniqueCode())
                .build();
        entity = repo.save(entity);
        return toResponse(entity);
    }

    public List<AppointmentResponse> listByStatus(Status status) {
        return repo.findByStatus(status).stream().map(this::toResponse).toList();
    }

    @Transactional
    public AppointmentResponse updateStatus(UUID id, Status status) {
        Appointment appt = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        appt.setStatus(status);
        return toResponse(appt);
    }

    private AppointmentResponse toResponse(Appointment a) {
        return new AppointmentResponse(
                a.getAppointmentId(),
                a.getPatientName(),
                a.getPhone(),
                a.getEmail(),
                a.getAppointmentTime(),
                a.getStatus(),
                a.getAppointmentCode(),
                a.getNotes(),
                a.getQuickBook(),
                a.getCreatedAt()
        );
    }

    private String generateUniqueCode() {
        String code;
        int attempts = 0;
        do {
            code = CodeGenerator.generate(8);
            attempts++;
            if (attempts > 100) throw new IllegalStateException("Cannot generate unique code");
        } while (repo.findByAppointmentCode(code).isPresent());
        return code;
    }
}
