package com.example.demo.controller.admin;

import com.example.demo.service.ExcelExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/admin/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatisticsController {

    @Autowired
    private ExcelExportService excelExportService;

    /**
     * Xuất thống kê ra Excel
     * @param period Loại: "day", "week", "month" (mặc định: "month")
     * @param date Ngày tham chiếu (mặc định: hôm nay)
     * @param startDate Ngày bắt đầu (tùy chọn, ưu tiên hơn period)
     * @param endDate Ngày kết thúc (tùy chọn, ưu tiên hơn period)
     * @return File Excel
     */
    @GetMapping("/export")
    public ResponseEntity<?> exportStatistics(
            @RequestParam(required = false, defaultValue = "month") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            LocalDate start;
            LocalDate end;
            String fileName;

            // Nếu có startDate và endDate, ưu tiên sử dụng
            if (startDate != null && endDate != null) {
                start = startDate;
                end = endDate;
                fileName = String.format("ThongKe_%s_%s.xlsx",
                    start.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                    end.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            } else {
                // Tính toán dựa trên period và date
                LocalDate referenceDate = date != null ? date : LocalDate.now();
                
                switch (period.toLowerCase()) {
                    case "day":
                        start = referenceDate;
                        end = referenceDate;
                        fileName = String.format("ThongKe_Ngay_%s.xlsx",
                            referenceDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                        break;
                    case "week":
                        // Tuần bắt đầu từ thứ 2
                        start = referenceDate.minusDays(referenceDate.getDayOfWeek().getValue() - 1);
                        end = start.plusDays(6);
                        fileName = String.format("ThongKe_Tuan_%s.xlsx",
                            start.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                        break;
                    case "month":
                    default:
                        start = referenceDate.withDayOfMonth(1);
                        end = referenceDate.withDayOfMonth(referenceDate.lengthOfMonth());
                        fileName = String.format("ThongKe_Thang_%s.xlsx",
                            referenceDate.format(DateTimeFormatter.ofPattern("yyyyMM")));
                        break;
                }
            }

            // Tạo file Excel
            byte[] excelData = excelExportService.exportStatisticsToExcel(start, end, period);

            // Tạo response
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(excelData.length);

            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tạo file Excel: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi: " + e.getMessage());
        }
    }
}

