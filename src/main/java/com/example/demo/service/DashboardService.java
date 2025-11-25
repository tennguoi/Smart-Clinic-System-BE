
// src/main/java/com/example/demo/service/DashboardService.java
package com.example.demo.service;

import com.example.demo.dto.CompletedMedicalRecordDTO;
import com.example.demo.dto.DashboardKPIResponse;
import com.example.demo.dto.DashboardStatsDTO;
import com.example.demo.dto.TopServiceAnalysisDTO;
import com.example.demo.dto.TopServiceDTO;
import com.example.demo.dto.TrendDataPoint;
import com.example.demo.entity.Appointment;
import com.example.demo.entity.MedicalRecordServiceEntity;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.BillingRecordRepository;
import com.example.demo.repository.InvoiceDetailRepository;
import com.example.demo.repository.MedicalRecordRepository;
import com.example.demo.repository.MedicalRecordServiceRepository;
import com.example.demo.repository.PatientRepository;
import com.example.demo.repository.ServiceRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private InvoiceDetailRepository invoiceDetailRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private BillingRecordRepository billingRecordRepository;

    @Autowired
    private UserRepository userRepository;

    // NEW: sử dụng để đếm số hồ sơ bệnh án theo thời gian
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private MedicalRecordServiceRepository medicalRecordServiceRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    /**
     * Top 5 dịch vụ theo doanh thu/số lượng (tùy implement trong InvoiceDetailRepository)
     */
    public List<TopServiceDTO> getTop5Services(String startDate, String endDate) {
        Pageable topFive = PageRequest.of(0, 5);
        
        // If date filters are provided, use them; otherwise use default behavior
        if (startDate != null && endDate != null) {
            try {
                LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
                LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
                return invoiceDetailRepository.findTopServicesBetween(start, end, topFive);
            } catch (Exception e) {
                // Fall back to default if date parsing fails
                return invoiceDetailRepository.findTopServices(topFive);
            }
        }
        
        return invoiceDetailRepository.findTopServices(topFive);
    }

    /**
     * Thống kê tổng quan (giữ nguyên, có thể xem là backward compatibility)
     */
    public DashboardStatsDTO getDashboardStats(String startDate, String endDate) {
        long totalAppointments = appointmentRepository.count();
        long totalPatients = patientRepository.count();
        // Sử dụng BillingRecordRepository để lấy doanh thu từ các hóa đơn đã thanh toán
        BigDecimal totalRevenue = billingRecordRepository.sumTotalRevenue();
        long totalDoctors = userRepository.countByRoleName("ROLE_BAC_SI");
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        return DashboardStatsDTO.builder()
                .totalAppointments(totalAppointments)
                .totalPatients(totalPatients)
                .totalRevenue(totalRevenue)
                .totalDoctors(totalDoctors)
                .build();
    }

    /**
     * KPI cho dashboard:
     * - totalAppointmentsToday: số lịch hẹn hôm nay
     * - appointmentsGrowth: % tăng/giảm so với hôm qua
     * - totalPatientsToday: (ĐÃ ĐỔI Ý NGHĨA) số hồ sơ bệnh án hôm nay
     * - patientsGrowth: (ĐÃ ĐỔI Ý NGHĨA) % tăng/giảm số hồ sơ bệnh án so với hôm qua
     * - totalRevenueMonth: doanh thu tháng hiện tại
     * - revenueGrowth: % tăng/giảm so với tháng trước
     * - cancelRate: tỷ lệ hủy lịch trong tháng hiện tại
     * - cancelRateGrowth: chênh lệch tỷ lệ hủy so với tháng trước (điểm phần trăm)
     */
    public DashboardKPIResponse getDashboardKPIs(String startDate, String endDate) {
        LocalDateTime now = LocalDateTime.now();

        // Mốc thời gian ngày
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = now; // có thể dùng end of day, nhưng để realtime thì dùng "now"
        LocalDateTime startOfYesterday = startOfToday.minusDays(1);
        LocalDateTime endOfYesterday = startOfToday.minusSeconds(1);

        // Mốc thời gian tháng
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfMonth.minusSeconds(1);

        // 1) Appointments Today
        long appointmentsToday = appointmentRepository.countByCreatedAtBetween(startOfToday, endOfToday);
        long appointmentsYesterday = appointmentRepository.countByCreatedAtBetween(startOfYesterday, endOfYesterday);
        double appointmentsGrowth = calculateGrowth(appointmentsToday, appointmentsYesterday);

        // 2) Medical Records Today (THAY CHO Patients Today)
        long medicalRecordsToday = medicalRecordRepository.countByCreatedAtBetween(startOfToday, endOfToday);
        long medicalRecordsYesterday = medicalRecordRepository.countByCreatedAtBetween(startOfYesterday, endOfYesterday);
        double medicalRecordsGrowth = calculateGrowth(medicalRecordsToday, medicalRecordsYesterday);

        // 3) Revenue Month - Sử dụng BillingRecordRepository để lấy doanh thu từ các hóa đơn đã thanh toán
        BigDecimal revenueMonth = billingRecordRepository.sumTotalRevenueBetween(startOfMonth, now);
        BigDecimal revenueLastMonth = billingRecordRepository.sumTotalRevenueBetween(startOfLastMonth, endOfLastMonth);
        if (revenueMonth == null) revenueMonth = BigDecimal.ZERO;
        if (revenueLastMonth == null) revenueLastMonth = BigDecimal.ZERO;
        double revenueGrowth = calculateGrowth(revenueMonth.doubleValue(), revenueLastMonth.doubleValue());

        // 4) Cancel Rate (Month)
        long totalAppointmentsMonth = appointmentRepository.countByCreatedAtBetween(startOfMonth, now);
        long cancelledAppointmentsMonth = appointmentRepository.countByStatusAndCreatedAtBetween(
                Appointment.Status.Cancelled, startOfMonth, now
        );

        long totalAppointmentsLastMonth = appointmentRepository.countByCreatedAtBetween(startOfLastMonth, endOfLastMonth);
        long cancelledAppointmentsLastMonth = appointmentRepository.countByStatusAndCreatedAtBetween(
                Appointment.Status.Cancelled, startOfLastMonth, endOfLastMonth
        );

        double cancelRateMonth = totalAppointmentsMonth == 0 ? 0.0 :
                (double) cancelledAppointmentsMonth / totalAppointmentsMonth * 100.0;
        double cancelRateLastMonth = totalAppointmentsLastMonth == 0 ? 0.0 :
                (double) cancelledAppointmentsLastMonth / totalAppointmentsLastMonth * 100.0;
        double cancelRateGrowth = cancelRateMonth - cancelRateLastMonth; // chênh lệch điểm %

        // Build response
        return DashboardKPIResponse.builder()
                .totalAppointmentsToday(appointmentsToday)
                .appointmentsGrowth(appointmentsGrowth)

                // GIỮ NGUYÊN TÊN FIELD, NHƯNG GIÁ TRỊ = SỐ HỒ SƠ BỆNH ÁN
                .totalPatientsToday(medicalRecordsToday)
                .patientsGrowth(medicalRecordsGrowth)

                .totalRevenueMonth(revenueMonth)
                .revenueGrowth(revenueGrowth)
                .cancelRate(cancelRateMonth)
                .cancelRateGrowth(cancelRateGrowth)
                .build();
    }

    /**
     * Trend số lịch hẹn theo ngày
     */
    public List<TrendDataPoint> getAppointmentTrend(String startDate, String endDate) {
        // If date filters are provided, use them; otherwise use default 7 days
        if (startDate != null && endDate != null) {
            try {
                LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
                LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
                return appointmentRepository.findTrendBetween(start, end);
            } catch (Exception e) {
                // Fall back to default if date parsing fails
                LocalDateTime end = LocalDateTime.now();
                LocalDateTime start = end.minusDays(7);
                return appointmentRepository.findTrendBetween(start, end);
            }
        }
        
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(7);
        return appointmentRepository.findTrendBetween(start, end);
    }

    /**
     * Trend doanh thu theo ngày - Sử dụng BillingRecordRepository để lấy doanh thu từ các hóa đơn đã thanh toán
     */
    public List<TrendDataPoint> getRevenueTrend(String startDate, String endDate) {
        // If date filters are provided, use them; otherwise use default 7 days
        if (startDate != null && endDate != null) {
            try {
                LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
                LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
                return billingRecordRepository.findRevenueTrendBetween(start, end);
            } catch (Exception e) {
                // Fall back to default if date parsing fails
                LocalDateTime end = LocalDateTime.now();
                LocalDateTime start = end.minusDays(7);
                return billingRecordRepository.findRevenueTrendBetween(start, end);
            }
        }
        
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(7);
        return billingRecordRepository.findRevenueTrendBetween(start, end);
    }

    /**
     * Helper: tính % tăng trưởng (current vs previous)
     * Nếu previous = 0, trả về 100 khi current > 0, ngược lại 0.
     */
    private double calculateGrowth(double current, double previous) {
        if (previous == 0) return current > 0 ? 100.0 : 0.0;
        return ((current - previous) / previous) * 100.0;
    }

    /**
     * ============ TOP 5 DỊCH VỤ - 3 LOẠI KHÁC NHAU ============
     */

    /**
     * A. Top dịch vụ theo lượt đặt lịch (Appointment)
     * → Đo nhu cầu khách hàng
     */
    public List<TopServiceAnalysisDTO.TopServiceItemDTO> getTopServicesByAppointments(String startDate, String endDate) {
        LocalDateTime start, end;
        
        if (startDate != null && endDate != null) {
            try {
                start = LocalDate.parse(startDate).atStartOfDay();
                end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
            } catch (Exception e) {
                end = LocalDateTime.now();
                start = end.minusDays(30);
            }
        } else {
            end = LocalDateTime.now();
            start = end.minusDays(30);
        }

        // Lấy tất cả Appointment trong khoảng thời gian
        List<Appointment> appointments = appointmentRepository.findAllBetween(start, end);

        // Đếm số lần mỗi service được đặt
        Map<String, Long> serviceCount = new HashMap<>();
        Map<String, com.example.demo.entity.Service> serviceMap = new HashMap<>();

        for (Appointment apt : appointments) {
            if (apt.getServiceIds() != null && !apt.getServiceIds().isEmpty()) {
                String[] serviceIds = apt.getServiceIds().split(",");
                for (String sid : serviceIds) {
                    String serviceId = sid.trim();
                    serviceCount.put(serviceId, serviceCount.getOrDefault(serviceId, 0L) + 1);
                    
                    // Lấy thông tin service nếu chưa có
                    if (!serviceMap.containsKey(serviceId)) {
                        serviceRepository.findById(serviceId).ifPresent(s -> serviceMap.put(serviceId, s));
                    }
                }
            }
        }

        // Chuyển đổi thành DTO và sắp xếp
        return serviceCount.entrySet().stream()
                .map(entry -> {
                    com.example.demo.entity.Service service = serviceMap.get(entry.getKey());
                    return TopServiceAnalysisDTO.TopServiceItemDTO.builder()
                            .serviceId(entry.getKey())
                            .serviceName(service != null ? service.getName() : "Unknown")
                            .serviceCategory(service != null ? service.getCategory().getDisplayName() : "Unknown")
                            .servicePrice(service != null ? service.getPrice() : BigDecimal.ZERO)
                            .photoUrl(service != null ? service.getPhotoUrl() : null)
                            .quantity(entry.getValue())
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getQuantity(), a.getQuantity()))
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * B. Top dịch vụ theo lượt khám thực tế (MedicalRecord)
     * → Đo hiệu suất hoạt động
     */
    public List<TopServiceAnalysisDTO.TopServiceItemDTO> getTopServicesByMedicalRecords(String startDate, String endDate) {
        LocalDateTime start, end;
        
        if (startDate != null && endDate != null) {
            try {
                start = LocalDate.parse(startDate).atStartOfDay();
                end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
            } catch (Exception e) {
                end = LocalDateTime.now();
                start = end.minusDays(30);
            }
        } else {
            end = LocalDateTime.now();
            start = end.minusDays(30);
        }

        // Lấy tất cả dịch vụ từ hồ sơ bệnh án hoàn thành
        List<MedicalRecordServiceEntity> services = medicalRecordServiceRepository
                .findCompletedRecordsWithServices(start, end);

        // Nhóm theo service và tính tổng quantity
        Map<String, Long> serviceCount = new HashMap<>();
        Map<String, com.example.demo.entity.Service> serviceMap = new HashMap<>();

        for (MedicalRecordServiceEntity mrs : services) {
            String serviceId = mrs.getService().getServiceId();
            serviceCount.put(serviceId, serviceCount.getOrDefault(serviceId, 0L) + mrs.getQuantity());
            serviceMap.put(serviceId, mrs.getService());
        }

        // Chuyển đổi thành DTO và sắp xếp
        return serviceCount.entrySet().stream()
                .map(entry -> {
                    com.example.demo.entity.Service service = serviceMap.get(entry.getKey());
                    return TopServiceAnalysisDTO.TopServiceItemDTO.builder()
                            .serviceId(entry.getKey())
                            .serviceName(service.getName())
                            .serviceCategory(service.getCategory().getDisplayName())
                            .servicePrice(service.getPrice())
                            .photoUrl(service.getPhotoUrl())
                            .quantity(entry.getValue())
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getQuantity(), a.getQuantity()))
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * C. Top dịch vụ theo doanh thu (Invoice)
     * → Đo hiệu quả tài chính
     */
    public List<TopServiceAnalysisDTO.TopServiceItemDTO> getTopServicesByRevenue(String startDate, String endDate) {
        LocalDateTime start, end;
        
        if (startDate != null && endDate != null) {
            try {
                start = LocalDate.parse(startDate).atStartOfDay();
                end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
            } catch (Exception e) {
                end = LocalDateTime.now();
                start = end.minusDays(30);
            }
        } else {
            end = LocalDateTime.now();
            start = end.minusDays(30);
        }

        Pageable topFive = PageRequest.of(0, 5);
        List<TopServiceDTO> topServices = invoiceDetailRepository.findTopServicesByRevenueBetween(start, end, topFive);

        return topServices.stream()
                .map(dto -> TopServiceAnalysisDTO.TopServiceItemDTO.builder()
                        .serviceId(dto.getService().getServiceId())
                        .serviceName(dto.getService().getName())
                        .serviceCategory(dto.getService().getCategory().getDisplayName())
                        .servicePrice(dto.getService().getPrice())
                        .photoUrl(dto.getService().getPhotoUrl())
                        .quantity(dto.getTotalUsage())
                        .totalRevenue(dto.getService().getPrice().multiply(BigDecimal.valueOf(dto.getTotalUsage())))
                        .avgPrice(dto.getService().getPrice())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách hồ sơ bệnh án hoàn thành cùng với các dịch vụ đi kèm
     * Dùng để theo dõi top 5 dịch vụ
     */
    public List<CompletedMedicalRecordDTO> getCompletedMedicalRecordsWithServices(String startDate, String endDate) {
        LocalDateTime start, end;
        
        if (startDate != null && endDate != null) {
            try {
                start = LocalDate.parse(startDate).atStartOfDay();
                end = LocalDate.parse(endDate).atTime(LocalTime.MAX);
            } catch (Exception e) {
                // Fall back to default if date parsing fails
                end = LocalDateTime.now();
                start = end.minusDays(30);
            }
        } else {
            // Default: last 30 days
            end = LocalDateTime.now();
            start = end.minusDays(30);
        }

        // Lấy tất cả các dịch vụ từ hồ sơ bệnh án hoàn thành
        List<MedicalRecordServiceEntity> services = medicalRecordServiceRepository
                .findCompletedRecordsWithServices(start, end);

        // Nhóm theo hồ sơ bệnh án
        Map<java.util.UUID, List<MedicalRecordServiceEntity>> groupedByRecord = services.stream()
                .collect(Collectors.groupingBy(s -> s.getMedicalRecord().getRecordId()));

        // Chuyển đổi thành DTO
        return groupedByRecord.entrySet().stream()
                .map(entry -> {
                    List<MedicalRecordServiceEntity> recordServices = entry.getValue();
                    MedicalRecordServiceEntity firstService = recordServices.get(0);
                    com.example.demo.entity.MedicalRecord record = firstService.getMedicalRecord();

                    BigDecimal totalAmount = recordServices.stream()
                            .map(s -> s.getUnitPrice().multiply(BigDecimal.valueOf(s.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    List<CompletedMedicalRecordDTO.ServiceItemDTO> serviceItems = recordServices.stream()
                            .map(s -> CompletedMedicalRecordDTO.ServiceItemDTO.builder()
                                    .serviceId(s.getService().getServiceId())
                                    .serviceName(s.getService().getName())
                                    .serviceCategory(s.getService().getCategory().getDisplayName())
                                    .quantity(s.getQuantity())
                                    .unitPrice(s.getUnitPrice())
                                    .totalPrice(s.getUnitPrice().multiply(BigDecimal.valueOf(s.getQuantity())))
                                    .note(s.getNote())
                                    .build())
                            .collect(Collectors.toList());

                    String patientName = record.getPatientName();
                    if ((patientName == null || patientName.trim().isEmpty()) && record.getPatient() != null) {
                        patientName = record.getPatient().getFullName();
                    }

                    return CompletedMedicalRecordDTO.builder()
                            .recordId(record.getRecordId())
                            .patientId(record.getPatient() != null ? record.getPatient().getPatientId() : null)
                            .patientName(patientName)
                            .doctorId(record.getDoctor() != null ? record.getDoctor().getUserId() : null)
                            .doctorName(record.getDoctor() != null ? record.getDoctor().getFullName() : null)
                            .diagnosis(record.getDiagnosis())
                            .treatmentNotes(record.getTreatmentNotes())
                            .createdAt(record.getCreatedAt())
                            .services(serviceItems)
                            .totalAmount(totalAmount)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
