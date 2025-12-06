package com.example.demo.service;

import com.example.demo.dto.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class ExcelExportService {

    @Autowired
    private DashboardService dashboardService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Xuất thống kê ra Excel theo khoảng thời gian
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @param periodType Loại: "day", "week", "month"
     * @return Byte array của file Excel
     */
    public byte[] exportStatisticsToExcel(LocalDate startDate, LocalDate endDate, String periodType) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        
        // Tạo các style
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle numberStyle = createNumberStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        // Sheet 1: Tổng quan (KPI)
        createOverviewSheet(workbook, startDate, endDate, headerStyle, titleStyle, numberStyle, currencyStyle);

        // Sheet 2: Doanh thu theo ngày
        createRevenueTrendSheet(workbook, startDate, endDate, headerStyle, titleStyle, currencyStyle, dateStyle);

        // Sheet 3: Top dịch vụ
        createTopServicesSheet(workbook, startDate, endDate, headerStyle, titleStyle, numberStyle, currencyStyle);

        // Sheet 4: Lịch hẹn theo ngày
        createAppointmentTrendSheet(workbook, startDate, endDate, headerStyle, titleStyle, numberStyle, dateStyle);

        // Sheet 5: Hồ sơ bệnh án hoàn thành
        createMedicalRecordsSheet(workbook, startDate, endDate, headerStyle, titleStyle, numberStyle, currencyStyle, dateStyle);

        // Ghi workbook vào ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }

    private void createOverviewSheet(Workbook workbook, LocalDate startDate, LocalDate endDate,
                                     CellStyle headerStyle, CellStyle titleStyle, 
                                     CellStyle numberStyle, CellStyle currencyStyle) {
        Sheet sheet = workbook.createSheet("Tổng quan");
        
        int rowNum = 0;
        
        // Tiêu đề
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BÁO CÁO TỔNG QUAN");
        titleCell.setCellStyle(titleStyle);
        
        // Thông tin khoảng thời gian
        Row periodRow = sheet.createRow(rowNum++);
        periodRow.createCell(0).setCellValue("Khoảng thời gian:");
        periodRow.createCell(1).setCellValue(startDate.format(DATE_FORMATTER) + " - " + endDate.format(DATE_FORMATTER));
        
        rowNum++; // Dòng trống
        
        // Lấy dữ liệu KPI
        String startDateStr = startDate.format(DateTimeFormatter.ISO_DATE);
        String endDateStr = endDate.format(DateTimeFormatter.ISO_DATE);
        DashboardKPIResponse kpi = dashboardService.getDashboardKPIs(startDateStr, endDateStr);
        DashboardStatsDTO stats = dashboardService.getDashboardStats(startDateStr, endDateStr);
        
        // Header
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Chỉ số");
        headerRow.createCell(1).setCellValue("Giá trị");
        headerRow.getCell(0).setCellStyle(headerStyle);
        headerRow.getCell(1).setCellStyle(headerStyle);
        
        // Dữ liệu
        createDataRow(sheet, rowNum++, "Tổng số lịch hẹn", String.valueOf(stats.getTotalAppointments()), numberStyle);
        createDataRow(sheet, rowNum++, "Lịch hẹn hôm nay", String.valueOf(kpi.getTotalAppointmentsToday()), numberStyle);
        createDataRow(sheet, rowNum++, "Tăng trưởng lịch hẹn (%)", String.format("%.2f", kpi.getAppointmentsGrowth()), numberStyle);
        
        rowNum++; // Dòng trống
        
        createDataRow(sheet, rowNum++, "Tổng số bệnh nhân", String.valueOf(stats.getTotalPatients()), numberStyle);
        createDataRow(sheet, rowNum++, "Hồ sơ hôm nay", String.valueOf(kpi.getTotalPatientsToday()), numberStyle);
        createDataRow(sheet, rowNum++, "Tăng trưởng hồ sơ (%)", String.format("%.2f", kpi.getPatientsGrowth()), numberStyle);
        
        rowNum++; // Dòng trống
        
        createDataRow(sheet, rowNum++, "Tổng doanh thu", formatCurrency(stats.getTotalRevenue()), currencyStyle);
        createDataRow(sheet, rowNum++, "Doanh thu tháng", formatCurrency(kpi.getTotalRevenueMonth()), currencyStyle);
        createDataRow(sheet, rowNum++, "Tăng trưởng doanh thu (%)", String.format("%.2f", kpi.getRevenueGrowth()), numberStyle);
        
        rowNum++; // Dòng trống
        
        createDataRow(sheet, rowNum++, "Tỷ lệ hủy lịch (%)", String.format("%.2f", kpi.getCancelRate()), numberStyle);
        createDataRow(sheet, rowNum++, "Tăng trưởng tỷ lệ hủy (%)", String.format("%.2f", kpi.getCancelRateGrowth()), numberStyle);
        
        createDataRow(sheet, rowNum++, "Tổng số bác sĩ", String.valueOf(stats.getTotalDoctors()), numberStyle);
        
        // Auto-size columns
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createRevenueTrendSheet(Workbook workbook, LocalDate startDate, LocalDate endDate,
                                         CellStyle headerStyle, CellStyle titleStyle,
                                         CellStyle currencyStyle, CellStyle dateStyle) {
        Sheet sheet = workbook.createSheet("Doanh thu theo ngày");
        
        int rowNum = 0;
        
        // Tiêu đề
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("DOANH THU THEO NGÀY");
        titleCell.setCellStyle(titleStyle);
        
        // Header
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Ngày");
        headerRow.createCell(1).setCellValue("Doanh thu");
        headerRow.getCell(0).setCellStyle(headerStyle);
        headerRow.getCell(1).setCellStyle(headerStyle);
        
        // Lấy dữ liệu
        String startDateStr = startDate.format(DateTimeFormatter.ISO_DATE);
        String endDateStr = endDate.format(DateTimeFormatter.ISO_DATE);
        List<TrendDataPoint> revenueTrend = dashboardService.getRevenueTrend(startDateStr, endDateStr);
        
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (TrendDataPoint point : revenueTrend) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(point.getDate());
            row.getCell(0).setCellStyle(dateStyle);
            
            BigDecimal revenue = BigDecimal.ZERO;
            if (point.getValue() != null) {
                if (point.getValue() instanceof BigDecimal) {
                    revenue = (BigDecimal) point.getValue();
                } else if (point.getValue() instanceof Number) {
                    revenue = BigDecimal.valueOf(point.getValue().doubleValue());
                }
            }
            row.createCell(1).setCellValue(revenue.doubleValue());
            row.getCell(1).setCellStyle(currencyStyle);
            totalRevenue = totalRevenue.add(revenue);
        }
        
        // Tổng
        rowNum++;
        Row totalRow = sheet.createRow(rowNum);
        totalRow.createCell(0).setCellValue("TỔNG CỘNG");
        totalRow.getCell(0).setCellStyle(headerStyle);
        totalRow.createCell(1).setCellValue(totalRevenue.doubleValue());
        totalRow.getCell(1).setCellStyle(currencyStyle);
        
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createTopServicesSheet(Workbook workbook, LocalDate startDate, LocalDate endDate,
                                         CellStyle headerStyle, CellStyle titleStyle,
                                         CellStyle numberStyle, CellStyle currencyStyle) {
        Sheet sheet = workbook.createSheet("Top dịch vụ");
        
        int rowNum = 0;
        
        // Tiêu đề
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("TOP 5 DỊCH VỤ THEO DOANH THU");
        titleCell.setCellStyle(titleStyle);
        
        // Header
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("STT");
        headerRow.createCell(1).setCellValue("Tên dịch vụ");
        headerRow.createCell(2).setCellValue("Danh mục");
        headerRow.createCell(3).setCellValue("Số lượng");
        headerRow.createCell(4).setCellValue("Đơn giá");
        headerRow.createCell(5).setCellValue("Tổng doanh thu");
        
        for (int i = 0; i < 6; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }
        
        // Lấy dữ liệu
        String startDateStr = startDate.format(DateTimeFormatter.ISO_DATE);
        String endDateStr = endDate.format(DateTimeFormatter.ISO_DATE);
        List<TopServiceAnalysisDTO.TopServiceItemDTO> topServices = 
            dashboardService.getTopServicesByRevenue(startDateStr, endDateStr);
        
        int stt = 1;
        for (TopServiceAnalysisDTO.TopServiceItemDTO service : topServices) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(stt++);
            row.getCell(0).setCellStyle(numberStyle);
            row.createCell(1).setCellValue(service.getServiceName());
            row.createCell(2).setCellValue(service.getServiceCategory());
            row.createCell(3).setCellValue(service.getQuantity());
            row.getCell(3).setCellStyle(numberStyle);
            row.createCell(4).setCellValue(service.getServicePrice().doubleValue());
            row.getCell(4).setCellStyle(currencyStyle);
            
            BigDecimal totalRevenue = service.getTotalRevenue() != null 
                ? service.getTotalRevenue() 
                : service.getServicePrice().multiply(BigDecimal.valueOf(service.getQuantity()));
            row.createCell(5).setCellValue(totalRevenue.doubleValue());
            row.getCell(5).setCellStyle(currencyStyle);
        }
        
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        sheet.autoSizeColumn(3);
        sheet.autoSizeColumn(4);
        sheet.autoSizeColumn(5);
    }

    private void createAppointmentTrendSheet(Workbook workbook, LocalDate startDate, LocalDate endDate,
                                             CellStyle headerStyle, CellStyle titleStyle,
                                             CellStyle numberStyle, CellStyle dateStyle) {
        Sheet sheet = workbook.createSheet("Lịch hẹn theo ngày");
        
        int rowNum = 0;
        
        // Tiêu đề
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("LỊCH HẸN THEO NGÀY");
        titleCell.setCellStyle(titleStyle);
        
        // Header
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Ngày");
        headerRow.createCell(1).setCellValue("Số lượng");
        headerRow.getCell(0).setCellStyle(headerStyle);
        headerRow.getCell(1).setCellStyle(headerStyle);
        
        // Lấy dữ liệu
        String startDateStr = startDate.format(DateTimeFormatter.ISO_DATE);
        String endDateStr = endDate.format(DateTimeFormatter.ISO_DATE);
        List<TrendDataPoint> appointmentTrend = dashboardService.getAppointmentTrend(startDateStr, endDateStr);
        
        long totalAppointments = 0;
        for (TrendDataPoint point : appointmentTrend) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(point.getDate());
            row.getCell(0).setCellStyle(dateStyle);
            
            long count = 0;
            if (point.getValue() != null) {
                if (point.getValue() instanceof Long) {
                    count = point.getValue().longValue();
                } else if (point.getValue() instanceof Number) {
                    count = point.getValue().longValue();
                }
            }
            row.createCell(1).setCellValue(count);
            row.getCell(1).setCellStyle(numberStyle);
            totalAppointments += count;
        }
        
        // Tổng
        rowNum++;
        Row totalRow = sheet.createRow(rowNum);
        totalRow.createCell(0).setCellValue("TỔNG CỘNG");
        totalRow.getCell(0).setCellStyle(headerStyle);
        totalRow.createCell(1).setCellValue(totalAppointments);
        totalRow.getCell(1).setCellStyle(numberStyle);
        
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createMedicalRecordsSheet(Workbook workbook, LocalDate startDate, LocalDate endDate,
                                            CellStyle headerStyle, CellStyle titleStyle,
                                            CellStyle numberStyle, CellStyle currencyStyle, CellStyle dateStyle) {
        Sheet sheet = workbook.createSheet("Hồ sơ bệnh án");
        
        int rowNum = 0;
        
        // Tiêu đề
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("HỒ SƠ BỆNH ÁN HOÀN THÀNH");
        titleCell.setCellStyle(titleStyle);
        
        // Header
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("STT");
        headerRow.createCell(1).setCellValue("Mã hồ sơ");
        headerRow.createCell(2).setCellValue("Bệnh nhân");
        headerRow.createCell(3).setCellValue("Bác sĩ");
        headerRow.createCell(4).setCellValue("Chẩn đoán");
        headerRow.createCell(5).setCellValue("Ngày tạo");
        headerRow.createCell(6).setCellValue("Tổng tiền");
        
        for (int i = 0; i < 7; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }
        
        // Lấy dữ liệu
        String startDateStr = startDate.format(DateTimeFormatter.ISO_DATE);
        String endDateStr = endDate.format(DateTimeFormatter.ISO_DATE);
        List<CompletedMedicalRecordDTO> records = 
            dashboardService.getCompletedMedicalRecordsWithServices(startDateStr, endDateStr);
        
        int stt = 1;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CompletedMedicalRecordDTO record : records) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(stt++);
            row.getCell(0).setCellStyle(numberStyle);
            row.createCell(1).setCellValue(record.getRecordId().toString().substring(0, 8).toUpperCase());
            row.createCell(2).setCellValue(record.getPatientName() != null ? record.getPatientName() : "");
            row.createCell(3).setCellValue(record.getDoctorName() != null ? record.getDoctorName() : "");
            row.createCell(4).setCellValue(record.getDiagnosis() != null ? record.getDiagnosis() : "");
            
            if (record.getCreatedAt() != null) {
                row.createCell(5).setCellValue(record.getCreatedAt().format(DATETIME_FORMATTER));
                row.getCell(5).setCellStyle(dateStyle);
            } else {
                row.createCell(5).setCellValue("");
            }
            
            BigDecimal amount = record.getTotalAmount() != null ? record.getTotalAmount() : BigDecimal.ZERO;
            row.createCell(6).setCellValue(amount.doubleValue());
            row.getCell(6).setCellStyle(currencyStyle);
            totalAmount = totalAmount.add(amount);
        }
        
        // Tổng
        rowNum++;
        Row totalRow = sheet.createRow(rowNum);
        totalRow.createCell(0).setCellValue("TỔNG CỘNG");
        totalRow.getCell(0).setCellStyle(headerStyle);
        totalRow.createCell(6).setCellValue(totalAmount.doubleValue());
        totalRow.getCell(6).setCellStyle(currencyStyle);
        
        for (int i = 0; i < 7; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // Helper methods
    private void createDataRow(Sheet sheet, int rowNum, String label, String value, CellStyle valueStyle) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        if (valueStyle != null) {
            valueCell.setCellStyle(valueStyle);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00 \"₫\""));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("dd/mm/yyyy"));
        return style;
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0 ₫";
        }
        try {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return formatter.format(amount);
        } catch (Exception e) {
            return amount.toPlainString() + " ₫";
        }
    }
}

