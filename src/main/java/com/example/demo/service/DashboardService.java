package com.example.demo.service;

import com.example.demo.dto.DashboardKPIResponse;
import com.example.demo.dto.TopServiceDTO;
import com.example.demo.dto.TrendDataPoint;
import com.example.demo.entity.Appointment;
import com.example.demo.repository.InvoiceDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private InvoiceDetailRepository invoiceDetailRepository;

    @Autowired
    private com.example.demo.repository.AppointmentRepository appointmentRepository;

    @Autowired
    private com.example.demo.repository.PatientRepository patientRepository;

    @Autowired
    private com.example.demo.repository.InvoiceRepository invoiceRepository;

    @Autowired
    private com.example.demo.repository.UserRepository userRepository;

    public List<TopServiceDTO> getTop5Services() {
        // Lấy 5 kết quả đầu tiên (Top 5)
        Pageable topFive = PageRequest.of(0, 5);
        return invoiceDetailRepository.findTopServices(topFive);
    }

    public com.example.demo.dto.DashboardStatsDTO getDashboardStats() {
        // Deprecated or keep for backward compatibility if needed
        long totalAppointments = appointmentRepository.count();
        long totalPatients = patientRepository.count();
        java.math.BigDecimal totalRevenue = invoiceRepository.sumTotalRevenue();
        long totalDoctors = userRepository.countByRoleName("ROLE_BAC_SI");

        if (totalRevenue == null) totalRevenue = java.math.BigDecimal.ZERO;

        return com.example.demo.dto.DashboardStatsDTO.builder()
                .totalAppointments(totalAppointments)
                .totalPatients(totalPatients)
                .totalRevenue(totalRevenue)
                .totalDoctors(totalDoctors)
                .build();
    }

    public com.example.demo.dto.DashboardKPIResponse getDashboardKPIs() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        java.time.LocalDateTime endOfToday = now; // or end of day

        java.time.LocalDateTime startOfYesterday = startOfToday.minusDays(1);
        java.time.LocalDateTime endOfYesterday = startOfToday.minusSeconds(1);

        java.time.LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        java.time.LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
        java.time.LocalDateTime endOfLastMonth = startOfMonth.minusSeconds(1);

        // 1. Appointments Today
        long appointmentsToday = appointmentRepository.countByCreatedAtBetween(startOfToday, endOfToday);
        long appointmentsYesterday = appointmentRepository.countByCreatedAtBetween(startOfYesterday, endOfYesterday);
        double appointmentsGrowth = calculateGrowth(appointmentsToday, appointmentsYesterday);

        // 2. Patients (New) Today - assuming "Total Patients" means new registrations or active
        // Let's use new registrations for "Today"
        long patientsToday = patientRepository.countByCreatedAtBetween(startOfToday, endOfToday);
        long patientsYesterday = patientRepository.countByCreatedAtBetween(startOfYesterday, endOfYesterday);
        double patientsGrowth = calculateGrowth(patientsToday, patientsYesterday);

        // 3. Revenue Month
        java.math.BigDecimal revenueMonth = invoiceRepository.sumTotalRevenueBetween(startOfMonth, now);
        java.math.BigDecimal revenueLastMonth = invoiceRepository.sumTotalRevenueBetween(startOfLastMonth, endOfLastMonth);
        if (revenueMonth == null) revenueMonth = java.math.BigDecimal.ZERO;
        if (revenueLastMonth == null) revenueLastMonth = java.math.BigDecimal.ZERO;
        double revenueGrowth = calculateGrowth(revenueMonth.doubleValue(), revenueLastMonth.doubleValue());

        // 4. Cancel Rate (Month)
        long totalAppointmentsMonth = appointmentRepository.countByCreatedAtBetween(startOfMonth, now);
        long cancelledAppointmentsMonth = appointmentRepository.countByStatusAndCreatedAtBetween(Appointment.Status.Cancelled, startOfMonth, now);
        
        long totalAppointmentsLastMonth = appointmentRepository.countByCreatedAtBetween(startOfLastMonth, endOfLastMonth);
        long cancelledAppointmentsLastMonth = appointmentRepository.countByStatusAndCreatedAtBetween(Appointment.Status.Cancelled, startOfLastMonth, endOfLastMonth);

        double cancelRateMonth = totalAppointmentsMonth == 0 ? 0 : (double) cancelledAppointmentsMonth / totalAppointmentsMonth * 100;
        double cancelRateLastMonth = totalAppointmentsLastMonth == 0 ? 0 : (double) cancelledAppointmentsLastMonth / totalAppointmentsLastMonth * 100;
        double cancelRateGrowth = cancelRateMonth - cancelRateLastMonth; // Percentage point difference

        return com.example.demo.dto.DashboardKPIResponse.builder()
                .totalAppointmentsToday(appointmentsToday)
                .appointmentsGrowth(appointmentsGrowth)
                .totalPatientsToday(patientsToday)
                .patientsGrowth(patientsGrowth)
                .totalRevenueMonth(revenueMonth)
                .revenueGrowth(revenueGrowth)
                .cancelRate(cancelRateMonth)
                .cancelRateGrowth(cancelRateGrowth)
                .build();
    }

    public List<com.example.demo.dto.TrendDataPoint> getAppointmentTrend(int days) {
        java.time.LocalDateTime end = java.time.LocalDateTime.now();
        java.time.LocalDateTime start = end.minusDays(days);
        return appointmentRepository.findTrendBetween(start, end);
    }

    public List<com.example.demo.dto.TrendDataPoint> getRevenueTrend(int days) {
        java.time.LocalDateTime end = java.time.LocalDateTime.now();
        java.time.LocalDateTime start = end.minusDays(days);
        return invoiceRepository.findRevenueTrendBetween(start, end);
    }

    private double calculateGrowth(double current, double previous) {
        if (previous == 0) return current > 0 ? 100.0 : 0.0;
        return ((current - previous) / previous) * 100.0;
    }
}