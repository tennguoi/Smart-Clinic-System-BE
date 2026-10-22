package com.example.demo.controller.admin;

import com.example.demo.dto.ClinicRequest;
import com.example.demo.dto.ClinicResponse;
import com.example.demo.service.ClinicService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.security.access.prepost.PreAuthorize;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;

@RestController
@RequestMapping("/api/admin/clinic")
@PreAuthorize("hasRole('ADMIN')")
public class AdminClinicController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminClinicController.class);
    private final ClinicService clinicService;
    private static final String UPLOAD_DIR = "uploads/logo";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long TARGET_FILE_SIZE = 2 * 1024 * 1024; // 2MB - mục tiêu sau khi resize
    private static final int MAX_IMAGE_WIDTH = 800; // Chiều rộng tối đa
    private static final int MAX_IMAGE_HEIGHT = 800; // Chiều cao tối đa
    
    public AdminClinicController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }
    
    /**
     * Lấy thông tin phòng khám
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ClinicResponse> getClinicInfo() {
        ClinicResponse response = clinicService.getClinicInfo();
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        // Thêm cache headers để đảm bảo luôn lấy dữ liệu mới nhất từ database
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(response);
    }
    
    /**
     * Cập nhật thông tin phòng khám
     */
    @PutMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateClinicInfo(@Valid @RequestBody ClinicRequest request) {
        logger.info("=== BẮT ĐẦU XỬ LÝ CẬP NHẬT THÔNG TIN PHÒNG KHÁM ===");
        logger.info("Request nhận được: {}", request);
        
        try {
            ClinicResponse response = clinicService.updateClinicInfo(request);
            logger.info("Cập nhật thành công! Response: {}", response);
            logger.info("=== KẾT THÚC XỬ LÝ CẬP NHẬT - THÀNH CÔNG ===");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Lỗi validation: {}", e.getMessage());
            logger.info("=== KẾT THÚC XỬ LÝ CẬP NHẬT - LỖI VALIDATION ===");
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Lỗi hệ thống khi cập nhật: ", e);
            logger.info("=== KẾT THÚC XỬ LÝ CẬP NHẬT - LỖI HỆ THỐNG ===");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi cập nhật thông tin phòng khám: " + e.getMessage()));
        }
    }
    
    /**
     * Endpoint debug - kiểm tra thông tin request
     */
    @PostMapping("/debug-update")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> debugUpdate(@RequestBody ClinicRequest request) {
        Map<String, Object> debug = new HashMap<>();
        debug.put("receivedRequest", request);
        debug.put("currentClinicInfo", clinicService.getClinicInfo());
        
        try {
            ClinicResponse updated = clinicService.updateClinicInfo(request);
            debug.put("updateSuccess", true);
            debug.put("updatedInfo", updated);
            
            // Đọc lại từ database để verify
            ClinicResponse verified = clinicService.getClinicInfo();
            debug.put("verifiedFromDB", verified);
            debug.put("isDataPersisted", verified != null && verified.equals(updated));
            
            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            debug.put("updateSuccess", false);
            debug.put("error", e.getMessage());
            debug.put("errorType", e.getClass().getSimpleName());
            return ResponseEntity.badRequest().body(debug);
        }
    }
    
    /**
     * Upload logo phòng khám
     */
    @PostMapping("/upload-logo")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> uploadLogo(@RequestParam("logo") MultipartFile logo) {
        if (logo.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "File logo không được để trống"));
        }
        
        // Kiểm tra định dạng file
        String contentType = logo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "File phải là hình ảnh"));
        }
        
        // Kiểm tra kích thước file
        long fileSize = logo.getSize();
        if (fileSize > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", 
                        String.format("File quá lớn. Vui lòng chọn file nhỏ hơn %dMB hoặc nén ảnh trước khi upload.", MAX_FILE_SIZE / (1024 * 1024))));
        }
        
        // Kiểm tra xem đã có thông tin phòng khám chưa
        ClinicResponse existingClinic = clinicService.getClinicInfo();
        if (existingClinic == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Vui lòng tạo thông tin phòng khám trước khi upload logo"));
        }
        
        try {
            // Tạo thư mục nếu chưa tồn tại
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Xóa logo cũ nếu có
            if (existingClinic.getLogoUrl() != null && existingClinic.getLogoUrl().startsWith("/uploads/logo/")) {
                try {
                    String oldFileName = existingClinic.getLogoUrl().substring("/uploads/logo/".length());
                    Path oldFilePath = uploadPath.resolve(oldFileName);
                    if (Files.exists(oldFilePath)) {
                        Files.delete(oldFilePath);
                    }
                } catch (IOException e) {
                    // Log warning nhưng không fail upload mới
                    logger.warn("Không thể xóa logo cũ: {}", e.getMessage());
                }
            }
            
            // Đọc toàn bộ ảnh vào memory để xử lý
            byte[] imageBytes = logo.getBytes();
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            
            if (originalImage == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Không thể đọc file ảnh"));
            }
            
            // Kiểm tra xem có cần resize không
            boolean needsResize = fileSize > TARGET_FILE_SIZE || 
                                 originalImage.getWidth() > MAX_IMAGE_WIDTH || 
                                 originalImage.getHeight() > MAX_IMAGE_HEIGHT;
            
            InputStream imageInputStream;
            String fileExtension = ".png"; // Mặc định là PNG
            
            if (needsResize) {
                // Resize ảnh để giảm kích thước file
                ResizeResult resizeResult = resizeImage(new ByteArrayInputStream(imageBytes), contentType);
                imageInputStream = resizeResult.getInputStream();
                fileExtension = resizeResult.getExtension();
            } else {
                // Sử dụng ảnh gốc, giữ nguyên format
                String originalFilename = logo.getOriginalFilename();
                if (originalFilename != null && 
                    (originalFilename.toLowerCase().endsWith(".jpg") || 
                     originalFilename.toLowerCase().endsWith(".jpeg"))) {
                    fileExtension = ".jpg";
                } else {
                    fileExtension = ".png";
                }
                imageInputStream = new ByteArrayInputStream(imageBytes);
            }
            
            // Tạo tên file unique
            String fileName = "clinic_logo_" + UUID.randomUUID().toString() + fileExtension;
            
            // Lưu file
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(imageInputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Tạo URL để lưu vào database
            String logoUrl = "/uploads/logo/" + fileName;
            
            // Cập nhật logoUrl vào database (chỉ update logoUrl, giữ nguyên các field khác)
            ClinicRequest updateRequest = new ClinicRequest();
            updateRequest.setName(existingClinic.getName()); // Giữ nguyên name để pass validation
            updateRequest.setLogoUrl(logoUrl);
            ClinicResponse response = clinicService.updateClinicInfo(updateRequest);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Upload logo thành công");
            result.put("logoUrl", logoUrl);
            result.put("clinicInfo", response);
            
            return ResponseEntity.ok(result);
            
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi upload file: " + e.getMessage()));
        }
    }
    
    /**
     * Class để lưu kết quả resize
     */
    private static class ResizeResult {
        private final InputStream inputStream;
        private final String extension;
        
        public ResizeResult(InputStream inputStream, String extension) {
            this.inputStream = inputStream;
            this.extension = extension;
        }
        
        public InputStream getInputStream() {
            return inputStream;
        }
        
        public String getExtension() {
            return extension;
        }
    }
    
    /**
     * Resize ảnh để giảm kích thước file
     */
    private ResizeResult resizeImage(InputStream originalInputStream, String contentType) throws IOException {
        try {
            BufferedImage originalImage = ImageIO.read(originalInputStream);
            if (originalImage == null) {
                throw new IOException("Không thể đọc ảnh");
            }
            
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            
            // Tính toán kích thước mới giữ nguyên tỷ lệ
            int newWidth = originalWidth;
            int newHeight = originalHeight;
            
            if (originalWidth > MAX_IMAGE_WIDTH || originalHeight > MAX_IMAGE_HEIGHT) {
                double widthRatio = (double) MAX_IMAGE_WIDTH / originalWidth;
                double heightRatio = (double) MAX_IMAGE_HEIGHT / originalHeight;
                double ratio = Math.min(widthRatio, heightRatio);
                
                newWidth = (int) (originalWidth * ratio);
                newHeight = (int) (originalHeight * ratio);
            }
            
            // Tạo ảnh mới với kích thước đã resize
            // Kiểm tra xem ảnh gốc có alpha channel (transparency) không
            int imageType = originalImage.getTransparency() == BufferedImage.OPAQUE 
                ? BufferedImage.TYPE_INT_RGB 
                : BufferedImage.TYPE_INT_ARGB;
            
            Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, imageType);
            
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.drawImage(scaledImage, 0, 0, null);
            g2d.dispose();
            
            // Chuyển đổi sang byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String format = "png";
            
            // Sử dụng PNG cho logo để giữ chất lượng tốt
            // Nếu file vẫn quá lớn, có thể chuyển sang JPEG với chất lượng thấp hơn
            if (resizedImage.getWidth() * resizedImage.getHeight() > 500000) {
                // Với ảnh quá lớn, chuyển sang RGB và dùng JPEG
                BufferedImage jpegImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2dJpeg = jpegImage.createGraphics();
                try {
                    g2dJpeg.drawImage(resizedImage, 0, 0, null);
                    ImageIO.write(jpegImage, "jpg", baos);
                    format = "jpg";
                } finally {
                    g2dJpeg.dispose(); // Đảm bảo dispose ngay cả khi có exception
                }
            } else {
                // Với ảnh nhỏ hơn, dùng PNG để giữ chất lượng
                ImageIO.write(resizedImage, "png", baos);
            }
            
            String extension = format.equals("jpg") ? ".jpg" : ".png";
            return new ResizeResult(new ByteArrayInputStream(baos.toByteArray()), extension);
        } catch (Exception e) {
            throw new IOException("Lỗi khi resize ảnh: " + e.getMessage(), e);
        }
    }
}

