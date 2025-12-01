package com.example.demo.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.example.demo.entity.MedicalRecordServiceEntity;
import com.example.demo.entity.Patient;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.demo.entity.MedicalRecord;
import com.example.demo.entity.Prescription;
import com.example.demo.repository.MedicalRecordServiceRepository;
import com.example.demo.repository.PrescriptionRepository;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.borders.Border;
import com.example.demo.entity.BillingRecord;
import com.example.demo.entity.BillingDetail;
@Service
public class PdfExportService {

    private static final Logger logger = LoggerFactory.getLogger(PdfExportService.class);

    private final PrescriptionRepository prescriptionRepository;
    private final ClinicService clinicService;
    private final MedicalRecordServiceRepository medicalRecordServiceRepository;

    public PdfExportService(PrescriptionRepository prescriptionRepository,
                            ClinicService clinicService,
                            MedicalRecordServiceRepository medicalRecordServiceRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.clinicService = clinicService;
        this.medicalRecordServiceRepository = medicalRecordServiceRepository;
    }

    public byte[] generateMedicalRecordPdf(MedicalRecord record) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            document.setMargins(40, 40, 40, 40);

            // Try to load an embedded font that supports Vietnamese (UTF-8).
            PdfFont font = null;
            try {
                try (java.io.InputStream is = getClass().getResourceAsStream("/static/NotoSans-Regular.ttf")) {
                    if (is != null) {
                        byte[] fontBytes = is.readAllBytes();
                        font = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                    }
                }
                if (font == null) {
                    font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                }
                document.setFont(font);
            } catch (Exception fe) {
                // If font loading fails, continue with default font (may not render Vietnamese correctly).
            }

            // ========== 1. TÊN PHÒNG KHÁM (Căn giữa trang) ==========
            com.example.demo.entity.Clinic clinic = clinicService.getClinicEntity();

            // Tên phòng khám căn giữa trang
            if (clinic != null && clinic.getName() != null && !clinic.getName().isEmpty()) {
                Paragraph clinicNameCentered = new Paragraph(clinic.getName())
                        .setFontSize(16)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(10);
                document.add(clinicNameCentered);
            }

            // ========== 2. HEADER: Logo + Thông tin phòng khám ==========
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{20, 80}));
            headerTable.setWidth(UnitValue.createPercentValue(100));
            headerTable.setMarginBottom(15);
            headerTable.setBorder(Border.NO_BORDER); // Bỏ border cho toàn bộ bảng header

            // Logo (80x80px) - ưu tiên từ database, sau đó từ static folder
            Cell logoCell = new Cell();
            logoCell.setPadding(5);
            logoCell.setBorder(Border.NO_BORDER); // Bỏ border cho logo cell
            boolean logoLoaded = false;

            try {
                // Thử load logo từ database hoặc static folder
                String logoUrlFromDb = null;
                if (clinic != null && clinic.getLogoUrl() != null && !clinic.getLogoUrl().isEmpty()) {
                    logoUrlFromDb = clinic.getLogoUrl();
                    logger.info("📸 Logo URL from database: {}", logoUrlFromDb);
                } else {
                    logger.warn("⚠️ No logo URL found in database, will try default paths");
                }

                // Danh sách các đường dẫn để thử load logo
                java.util.List<String> logoPaths = new java.util.ArrayList<>();

                // 1. Từ database (nếu có)
                if (logoUrlFromDb != null) {
                    logoPaths.add(logoUrlFromDb);
                    // Nếu logoUrl bắt đầu bằng "/uploads/", thêm biến thể không có dấu "/"
                    if (logoUrlFromDb.startsWith("/uploads/")) {
                        logoPaths.add(logoUrlFromDb.substring(1)); // Bỏ dấu "/" đầu tiên
                    }
                }

                // 2. Các đường dẫn mặc định
                logoPaths.add("/static/logo.png");
                logoPaths.add("static/logo.png");
                logoPaths.add("uploads/logo.png");
                logoPaths.add("./uploads/logo.png");
                
                logger.debug("📋 Will try loading logo from {} paths: {}", logoPaths.size(), logoPaths);
                
                // Thử load từ từng đường dẫn
                for (String logoPath : logoPaths) {
                    if (logoLoaded) break;

                    try {
                        // Nếu là URL đầy đủ (http/https)
                        if (logoPath.startsWith("http://") || logoPath.startsWith("https://")) {
                            try {
                                logger.debug("Trying to load logo from URL: {}", logoPath);
                                java.net.URL url = new java.net.URL(logoPath);
                                Image logo = new Image(ImageDataFactory.create(url));
                                logo.setWidth(80);
                                logo.setHeight(80);
                                logoCell.add(logo);
                                logoLoaded = true;
                                logger.info("Logo loaded successfully from URL: {}", logoPath);
                                break;
                            } catch (Exception urlEx) {
                                logger.debug("Failed to load logo from URL {}: {}", logoPath, urlEx.getMessage());
                            }
                        }
                        // Nếu là đường dẫn resource (static folder)
                        else if (logoPath.startsWith("/static/") || logoPath.startsWith("static/") || logoPath.equals("/static/logo.png")) {
                            String resourcePath = logoPath.startsWith("/") ? logoPath : "/" + logoPath;
                            // Thử nhiều biến thể
                            String[] resourceVariants = {
                                    resourcePath,
                                    resourcePath.replace("/static/", "/"),
                                    "static/" + logoPath.replace("/static/", "").replace("static/", "")
                            };

                            for (String variant : resourceVariants) {
                                if (logoLoaded) break;
                                try {
                                    logger.debug("Trying to load logo from resource: {}", variant);
                                    java.io.InputStream logoStream = getClass().getResourceAsStream(variant);
                                    if (logoStream != null) {
                                        byte[] logoBytes = logoStream.readAllBytes();
                                        if (logoBytes.length > 0) {
                                            Image logo = new Image(ImageDataFactory.create(logoBytes));
                                            logo.setWidth(80);
                                            logo.setHeight(80);
                                            logoCell.add(logo);
                                            logoLoaded = true;
                                            logger.info("Logo loaded successfully from resource: {}", variant);
                                            break;
                                        }
                                        logoStream.close();
                                    }
                                } catch (Exception e) {
                                    logger.debug("Failed to load logo from resource {}: {}", variant, e.getMessage());
                                }
                            }
                        }
                        // Nếu là đường dẫn file system (uploads folder)
                        else if (logoPath.startsWith("uploads/") || logoPath.startsWith("./uploads/") || logoPath.startsWith("/uploads/")) {
                            try {
                                // Xử lý path: bỏ "./" hoặc "/" ở đầu
                                String filePath = logoPath;
                                if (filePath.startsWith("./")) {
                                    filePath = filePath.substring(2);
                                } else if (filePath.startsWith("/")) {
                                    filePath = filePath.substring(1);
                                }
                                
                                logger.debug("Trying to load logo from file: {}", filePath);
                                java.io.File logoFile = new java.io.File(filePath);
                                
                                if (logoFile.exists() && logoFile.isFile()) {
                                    Image logo = new Image(ImageDataFactory.create(logoFile.getAbsolutePath()));
                                    logo.setWidth(80);
                                    logo.setHeight(80);
                                    logoCell.add(logo);
                                    logoLoaded = true;
                                    logger.info("✅ Logo loaded successfully from file: {}", logoFile.getAbsolutePath());
                                    break;
                                } else {
                                    logger.debug("❌ Logo file does not exist: {} (absolute: {})", 
                                        filePath, logoFile.getAbsolutePath());
                                }
                            } catch (Exception fileEx) {
                                logger.debug("Failed to load logo from file {}: {}", logoPath, fileEx.getMessage());
                            }
                        }
                        // Nếu là đường dẫn file system tuyệt đối
                        else {
                            try {
                                logger.debug("Trying to load logo from absolute path: {}", logoPath);
                                java.io.File logoFile = new java.io.File(logoPath);
                                if (logoFile.exists() && logoFile.isFile()) {
                                    Image logo = new Image(ImageDataFactory.create(logoFile.getAbsolutePath()));
                                    logo.setWidth(80);
                                    logo.setHeight(80);
                                    logoCell.add(logo);
                                    logoLoaded = true;
                                    logger.info("Logo loaded successfully from absolute path: {}", logoFile.getAbsolutePath());
                                    break;
                                }
                            } catch (Exception fileEx) {
                                logger.debug("Failed to load logo from absolute path {}: {}", logoPath, fileEx.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        logger.debug("Error trying to load logo from {}: {}", logoPath, e.getMessage());
                    }
                }

                // Nếu vẫn chưa có logo, dùng placeholder
                if (!logoLoaded) {
                    logger.warn("Logo not found after trying all paths. Using placeholder. Please ensure logo.png exists in src/main/resources/static/ or uploads/");
                    Paragraph logoPlaceholder = new Paragraph("LOGO")
                            .setFontSize(10)
                            .setBold()
                            .setTextAlignment(TextAlignment.CENTER)
                            .setPadding(30);
                    logoCell.add(logoPlaceholder);
                    logoCell.setHeight(80);
                }
            } catch (Exception e) {
                logger.error("Error loading logo: {}", e.getMessage(), e);
                // Nếu có lỗi, dùng placeholder
                Paragraph logoPlaceholder = new Paragraph("LOGO")
                        .setFontSize(10)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(30);
                logoCell.add(logoPlaceholder);
                logoCell.setHeight(80);
            }
            headerTable.addCell(logoCell);

            // Thông tin phòng khám (xử lý null-safe) - không bao gồm tên (đã tách ra ở trên)
            Cell infoCell = new Cell();
            infoCell.setPadding(5);
            infoCell.setBorder(Border.NO_BORDER); // Bỏ border cho info cell

            if (clinic != null) {
                // Không thêm tên phòng khám vào đây nữa vì đã tách ra căn giữa ở trên

                if (clinic.getAddress() != null && !clinic.getAddress().isEmpty()) {
                    Paragraph clinicAddress = new Paragraph("Địa chỉ: " + clinic.getAddress())
                            .setFontSize(12)
                            .setMarginBottom(2);
                    infoCell.add(clinicAddress);
                }

                String contactInfo = "";
                if (clinic.getPhone() != null && !clinic.getPhone().isEmpty()) {
                    contactInfo += "SĐT: " + clinic.getPhone();
                }
                if (clinic.getEmail() != null && !clinic.getEmail().isEmpty()) {
                    if (!contactInfo.isEmpty()) contactInfo += " – ";
                    contactInfo += "Email: " + clinic.getEmail();
                }
                if (!contactInfo.isEmpty()) {
                    Paragraph clinicContact = new Paragraph(contactInfo)
                            .setFontSize(12)
                            .setMarginBottom(2);
                    infoCell.add(clinicContact);
                }

                if (clinic.getWebsite() != null && !clinic.getWebsite().isEmpty()) {
                    Paragraph clinicWebsite = new Paragraph("Website: " + clinic.getWebsite())
                            .setFontSize(12)
                            .setMarginBottom(2);
                    infoCell.add(clinicWebsite);
                }

                // Giờ làm việc
                String workingHours = "";
                if (clinic.getMorningStartTime() != null && clinic.getMorningEndTime() != null) {
                    workingHours += "Sáng: " + clinic.getMorningStartTime().toString() + " - " + clinic.getMorningEndTime().toString();
                }
                if (clinic.getAfternoonStartTime() != null && clinic.getAfternoonEndTime() != null) {
                    if (!workingHours.isEmpty()) workingHours += " | ";
                    workingHours += "Chiều: " + clinic.getAfternoonStartTime().toString() + " - " + clinic.getAfternoonEndTime().toString();
                }
                if (!workingHours.isEmpty()) {
                    Paragraph clinicWorkingHours = new Paragraph("Giờ làm việc: " + workingHours)
                            .setFontSize(12);
                    infoCell.add(clinicWorkingHours);
                }
            } else {
                // Nếu không có thông tin phòng khám, hiển thị thông báo
                Paragraph clinicName = new Paragraph("PHÒNG KHÁM")
                        .setFontSize(12)
                        .setBold()
                        .setMarginBottom(3);
                infoCell.add(clinicName);
                Paragraph notice = new Paragraph("Vui lòng cập nhật thông tin phòng khám trong hệ thống")
                        .setFontSize(10)
                        .setItalic();
                infoCell.add(notice);
            }

            headerTable.addCell(infoCell);
            document.add(headerTable);

            // ========== 2. TIÊU ĐỀ PHIẾU ==========
            Paragraph title = new Paragraph("PHIẾU KHÁM BỆNH")
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20)
                    .setMarginBottom(20);
            document.add(title);

            // ========== 3. THÔNG TIN CHUNG (Không có bảng) ==========
            String visitDate = record.getCreatedAt() != null ?
                    record.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Trống";
            String ticketNumber = "PK-" + record.getRecordId().toString().substring(0, 8).toUpperCase();
            String doctorName = record.getDoctor() != null ? record.getDoctor().getFullName() : "Trống";

            Paragraph infoLine = new Paragraph("Ngày khám: " + visitDate + "     Số phiếu: " + ticketNumber + "     Bác sĩ khám: BS. " + doctorName)
                    .setFontSize(12)
                    .setMarginBottom(15);
            document.add(infoLine);

            // Lấy thông tin bệnh nhân
            Patient patient = record.getPatient();

            String patientName = record.getPatientName();
            if ((patientName == null || patientName.trim().isEmpty()) && patient != null) {
                patientName = patient.getFullName();
            }
            patientName = (patientName != null && !patientName.trim().isEmpty()) ? patientName.trim() : "Trống";

            String patientPhone = (patient != null && patient.getPhone() != null && !patient.getPhone().isEmpty())
                    ? patient.getPhone()
                    : "Trống";
            String patientAddress = (patient != null && patient.getAddress() != null && !patient.getAddress().isEmpty())
                    ? patient.getAddress()
                    : "Trống";

            LocalDate patientDob = patient != null ? patient.getDob() : null;
            String patientDobStr = patientDob != null ? patientDob.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Trống";

            String patientGender = "Trống";
            if (patient != null && patient.getGender() != null) {
                patientGender = patient.getGender().getLabel();  // tự động trả về "Nam", "Nữ", "Khác"
            }

            // ========== 4. I. THÔNG TIN BỆNH NHÂN (Dạng gạch chấm) ==========
            Paragraph patientSection = new Paragraph("I. THÔNG TIN BỆNH NHÂN")
                    .setFontSize(12)
                    .setBold()
                    .setMarginTop(10)
                    .setMarginBottom(8);
            document.add(patientSection);

            document.add(createDottedLine("Họ tên:", patientName, 12));
            document.add(createDottedLine("Giới tính:", patientGender, 12));
            document.add(createDottedLine("Ngày sinh:", patientDobStr, 12));
            document.add(createDottedLine("SĐT:", patientPhone, 12));
            document.add(createDottedLine("Địa chỉ:", patientAddress, 12));

            // ========== 5. II. LÝ DO KHÁM / TRIỆU CHỨNG ==========
            Paragraph symptomSection = new Paragraph("II. LÝ DO KHÁM / TRIỆU CHỨNG")
                    .setFontSize(12)
                    .setBold()
                    .setMarginTop(15)
                    .setMarginBottom(8);
            document.add(symptomSection);

            // Lấy từ treatmentNotes hoặc để trống
            String symptoms = (patient != null && patient.getNotes() != null) ? patient.getNotes() : null;
            document.add(createDottedLine("Triệu chứng:", symptoms, 12));

            // ========== 6. III. CHẨN ĐOÁN ==========
            Paragraph diagnosisSection = new Paragraph("III. CHẨN ĐOÁN")
                    .setFontSize(12)
                    .setBold()
                    .setMarginTop(15)
                    .setMarginBottom(8);
            document.add(diagnosisSection);

            String diagnosisText = record.getDiagnosis() != null ? record.getDiagnosis() : null;
            document.add(createDottedLine("Chẩn đoán:", diagnosisText, 12));

            // ========== 7. IV. CHỈ ĐỊNH DỊCH VỤ (BẢNG 2 CỘT) ==========
            Paragraph serviceSection = new Paragraph("IV. CHỈ ĐỊNH DỊCH VỤ")
                    .setFontSize(12)
                    .setBold()
                    .setMarginTop(15)
                    .setMarginBottom(8);
            document.add(serviceSection);

            Table serviceTable = new Table(UnitValue.createPercentArray(new float[]{66.67f, 33.33f}));
            serviceTable.setWidth(UnitValue.createPercentValue(100));
            serviceTable.setMarginBottom(15);

            // Header
            Cell serviceHeader1 = new Cell();
            serviceHeader1.setPadding(5);
            serviceHeader1.add(new Paragraph("Dịch vụ").setBold().setFontSize(11));
            serviceHeader1.setBorder(new SolidBorder(new DeviceRgb(128, 128, 128), 0.5f));
            serviceTable.addCell(serviceHeader1);

            Cell serviceHeader2 = new Cell();
            serviceHeader2.setPadding(5);
            serviceHeader2.add(new Paragraph("Giá").setBold().setFontSize(11));
            serviceHeader2.setBorder(new SolidBorder(new DeviceRgb(128, 128, 128), 0.5f));
            serviceTable.addCell(serviceHeader2);

            List<MedicalRecordServiceEntity> serviceEntities =
                    medicalRecordServiceRepository.findAllByMedicalRecord_RecordId(record.getRecordId());

            if (serviceEntities.isEmpty()) {
                Cell emptyServiceCell = new Cell();
                emptyServiceCell.setPadding(5);
                emptyServiceCell.add(new Paragraph("Trống").setFontSize(11));
                emptyServiceCell.setBorder(new SolidBorder(new DeviceRgb(128, 128, 128), 0.5f));
                serviceTable.addCell(emptyServiceCell);

                Cell emptyPriceCell = new Cell();
                emptyPriceCell.setPadding(5);
                emptyPriceCell.add(new Paragraph("Trống").setFontSize(11));
                emptyPriceCell.setBorder(new SolidBorder(new DeviceRgb(128, 128, 128), 0.5f));
                serviceTable.addCell(emptyPriceCell);
            } else {
                for (MedicalRecordServiceEntity serviceEntity : serviceEntities) {
                    StringBuilder serviceInfo = new StringBuilder();
                    if (serviceEntity.getService() != null && serviceEntity.getService().getName() != null) {
                        serviceInfo.append(serviceEntity.getService().getName());
                    } else {
                        serviceInfo.append("Dịch vụ");
                    }

                    Integer quantity = serviceEntity.getQuantity() != null ? serviceEntity.getQuantity() : 1;
                    if (quantity > 1) {
                        serviceInfo.append(" x").append(quantity);
                    }

                    if (serviceEntity.getNote() != null && !serviceEntity.getNote().isBlank()) {
                        serviceInfo.append("\nGhi chú: ").append(serviceEntity.getNote());
                    }

                    Cell serviceCell = new Cell();
                    serviceCell.setPadding(5);
                    serviceCell.add(new Paragraph(serviceInfo.toString()).setFontSize(11));
                    serviceCell.setBorder(new SolidBorder(new DeviceRgb(128, 128, 128), 0.5f));
                    serviceTable.addCell(serviceCell);

                    BigDecimal unitPrice = serviceEntity.getUnitPrice() != null ? serviceEntity.getUnitPrice() : BigDecimal.ZERO;
                    BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
                    String priceText = formatCurrency(totalPrice);

                    Cell priceCell = new Cell();
                    priceCell.setPadding(5);
                    priceCell.add(new Paragraph(priceText).setFontSize(11));
                    priceCell.setBorder(new SolidBorder(new DeviceRgb(128, 128, 128), 0.5f));
                    serviceTable.addCell(priceCell);
                }
            }

            document.add(serviceTable);

            // ========== 8. V. ĐƠN THUỐC (Bảng 2 cột) ==========
            Paragraph prescriptionSection = new Paragraph("V. ĐƠN THUỐC")
                    .setFontSize(12)
                    .setBold()
                    .setMarginTop(15)
                    .setMarginBottom(8);
            document.add(prescriptionSection);

            Optional<Prescription> prescription = prescriptionRepository.findByRecordId(record.getRecordId());

            // Tạo bảng 2 cột: Thuốc và Hướng dẫn sử dụng
            Table prescriptionTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
            prescriptionTable.setWidth(UnitValue.createPercentValue(100));
            prescriptionTable.setMarginBottom(15);

            // Header
            Cell drugHeader = new Cell();
            drugHeader.setPadding(5);
            drugHeader.add(new Paragraph("Thuốc").setBold().setFontSize(11));
            drugHeader.setBorder(new SolidBorder(new DeviceRgb(128, 128, 128), 0.5f));
            prescriptionTable.addCell(drugHeader);

            Cell instructionHeader = new Cell();
            instructionHeader.setPadding(5);
            instructionHeader.add(new Paragraph("Hướng dẫn sử dụng").setBold().setFontSize(11));
            instructionHeader.setBorder(new SolidBorder(new DeviceRgb(128, 128, 128), 0.5f));
            prescriptionTable.addCell(instructionHeader);

            if (prescription.isPresent()) {
                Prescription rx = prescription.get();
                String drugs = rx.getDrugs() != null ? rx.getDrugs() : "";
                String instructions = rx.getInstructions() != null ? rx.getInstructions() : "";

                // Thêm dữ liệu vào bảng
                Cell drugCell = new Cell();
                drugCell.setPadding(5);
                drugCell.add(new Paragraph(drugs.isEmpty() ? "" : drugs).setFontSize(11));
                drugCell.setBorder(new SolidBorder(new DeviceRgb(128, 128, 128), 0.5f));
                prescriptionTable.addCell(drugCell);

                Cell instructionCell = new Cell();
                instructionCell.setPadding(5);
                instructionCell.add(new Paragraph(instructions.isEmpty() ? "" : instructions).setFontSize(11));
                instructionCell.setBorder(new SolidBorder(new DeviceRgb(128, 128, 128), 0.5f));
                prescriptionTable.addCell(instructionCell);
            } else {
                // Nếu không có đơn thuốc, để trống
                Cell drugCell = new Cell();
                drugCell.setPadding(5);
                drugCell.add(new Paragraph("").setFontSize(11));
                drugCell.setBorder(new SolidBorder(new DeviceRgb(128, 128, 128), 0.5f));
                prescriptionTable.addCell(drugCell);

                Cell instructionCell = new Cell();
                instructionCell.setPadding(5);
                instructionCell.add(new Paragraph("").setFontSize(11));
                instructionCell.setBorder(new SolidBorder(new DeviceRgb(128, 128, 128), 0.5f));
                prescriptionTable.addCell(instructionCell);
            }

            document.add(prescriptionTable);

            // ========== 9. CHỮ KÝ BÁC SĨ (Căn phải, không có bảng) ==========
            document.add(new Paragraph("\n\n"));

            Paragraph doctorLabel = new Paragraph("Bác sĩ khám bệnh")
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(5);
            document.add(doctorLabel);

            Paragraph doctorSubLabel = new Paragraph("(Ký và ghi rõ họ tên)")
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(50);
            document.add(doctorSubLabel);

            Paragraph doctorNameLine = new Paragraph(".......................................................")
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(doctorNameLine);

            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo PDF: " + e.getMessage(), e);
        }
    }

    public byte[] generatePrescriptionPdf(Prescription rx) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // reuse the same font-loading logic
            try {
                PdfFont font = null;
                try (java.io.InputStream is = getClass().getResourceAsStream("/fonts/NotoSans-Regular.ttf")) {
                    if (is != null) {
                        byte[] fontBytes = is.readAllBytes();
                        font = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                    }
                }
                if (font == null) {
                    try (java.io.InputStream is2 = getClass().getResourceAsStream("/fonts/DejaVuSans.ttf")) {
                        if (is2 != null) {
                            byte[] fontBytes2 = is2.readAllBytes();
                            font = PdfFontFactory.createFont(fontBytes2, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                        }
                    }
                }
                if (font == null) {
                    font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                }
                document.setFont(font);
            } catch (Exception fe) {
                // ignore font failure
            }

            Paragraph title = new Paragraph("ĐƠN THUỐC CHỈ ĐỊNH")
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            document.add(new Paragraph("\n"));

            Table info = new Table(UnitValue.createPercentArray(new float[]{1, 2}));
            info.setWidth(UnitValue.createPercentValue(100));
            Cell c1 = new Cell();
            c1.add(new Paragraph("Bệnh nhân:").setBold());
            info.addCell(c1);
            Cell c2 = new Cell();
            String patientName = rx.getMedicalRecord() != null && rx.getMedicalRecord().getPatient() != null
                    ? rx.getMedicalRecord().getPatient().getFullName() : "Trống";
            c2.add(new Paragraph(patientName != null ? patientName : "Trống"));
            info.addCell(c2);

            Cell c3 = new Cell();
            c3.add(new Paragraph("Bác sĩ:").setBold());
            info.addCell(c3);
            Cell c4 = new Cell();
            String doctorName = rx.getMedicalRecord() != null && rx.getMedicalRecord().getDoctor() != null
                    ? rx.getMedicalRecord().getDoctor().getFullName() : "Trống";
            c4.add(new Paragraph(doctorName != null ? doctorName : "Trống"));
            info.addCell(c4);

            Cell c5 = new Cell();
            c5.add(new Paragraph("Ngày kê đơn:").setBold());
            info.addCell(c5);
            Cell c6 = new Cell();
            c6.add(new Paragraph(rx.getIssuedAt() != null ? rx.getIssuedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Trống"));
            info.addCell(c6);

            document.add(info);
            document.add(new Paragraph("\n"));

            Paragraph drugsHeader = new Paragraph("Danh sách thuốc").setBold().setFontSize(12);
            document.add(drugsHeader);
            Table drugsTable = new Table(1);
            drugsTable.setWidth(UnitValue.createPercentValue(100));
            Cell drugsCell = new Cell();
            drugsCell.add(new Paragraph(rx.getDrugs() != null ? rx.getDrugs() : "Trống"));
            drugsTable.addCell(drugsCell);
            document.add(drugsTable);

            document.add(new Paragraph("\n"));
            Paragraph instrHeader = new Paragraph("Hướng dẫn sử dụng").setBold().setFontSize(12);
            document.add(instrHeader);
            Paragraph instr = new Paragraph(rx.getInstructions() != null ? rx.getInstructions() : "Trống").setFontSize(11);
            document.add(instr);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo PDF đơn thuốc: " + e.getMessage(), e);
        }
    }

    public byte[] generatePrescriptionPdfForRecord(MedicalRecord record) {
        Optional<Prescription> opt = prescriptionRepository.findByRecordId(record.getRecordId());
        Prescription rx = opt.orElseThrow(() -> new RuntimeException("Không tìm thấy đơn thuốc cho hồ sơ."));
        return generatePrescriptionPdf(rx);
    }

    private Paragraph createDottedLine(String label, String value, float fontSize) {
        String safeLabel = label != null ? label.trim() : "";
        boolean hasValue = value != null && !value.trim().isEmpty();

        if (safeLabel.isEmpty() && !hasValue) {
            return new Paragraph(" ").setFontSize(fontSize).setMarginBottom(5);
        }

        StringBuilder text = new StringBuilder();
        if (!safeLabel.isEmpty()) {
            text.append(safeLabel);
        }

        if (hasValue) {
            if (text.length() > 0) {
                text.append(" ");
            }
            text.append(value.trim());
        } else {
            if (text.length() > 0) {
                text.append(" ");
            }
            text.append("Trống");
        }

        return new Paragraph(text.toString()).setFontSize(fontSize).setMarginBottom(5);
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "Trống";
        }
        try {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return formatter.format(amount);
        } catch (Exception e) {
            return amount.stripTrailingZeros().toPlainString();
        }
    }
    public byte[] generateInvoicePdf(BillingRecord bill) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            document.setMargins(20, 40, 20, 40); // Căn lề

            // 1. Cài đặt Font (Tái sử dụng logic cũ để hỗ trợ tiếng Việt)
            try {
                PdfFont font = null;
                try (java.io.InputStream is = getClass().getResourceAsStream("/static/NotoSans-Regular.ttf")) {
                    if (is != null) {
                        byte[] fontBytes = is.readAllBytes();
                        font = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                    }
                }
                if (font == null) {
                    font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                }
                document.setFont(font);
            } catch (Exception fe) {
                // Fallback font
            }

            // 2. Header (Thông tin phòng khám)
            com.example.demo.entity.Clinic clinic = clinicService.getClinicEntity();
            String clinicName = (clinic != null && clinic.getName() != null) ? clinic.getName() : "PHÒNG KHÁM ĐA KHOA";
            String clinicAddress = (clinic != null && clinic.getAddress() != null) ? clinic.getAddress() : "Địa chỉ: ...";
            String clinicPhone = (clinic != null && clinic.getPhone() != null) ? clinic.getPhone() : "...";

            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1}));
            headerTable.setWidth(UnitValue.createPercentValue(100));
            headerTable.addCell(new Cell().add(new Paragraph(clinicName).setBold().setFontSize(14).setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));
            headerTable.addCell(new Cell().add(new Paragraph(clinicAddress).setFontSize(10).setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));
            headerTable.addCell(new Cell().add(new Paragraph("Hotline: " + clinicPhone).setFontSize(10).setTextAlignment(TextAlignment.CENTER)).setBorder(Border.NO_BORDER));
            document.add(headerTable);

            document.add(new Paragraph("\n")); // Khoảng trắng

            // 3. Tiêu đề Hóa đơn
            Paragraph title = new Paragraph("HÓA ĐƠN THANH TOÁN")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(title);

            // 4. Thông tin chung (2 cột)
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
            infoTable.setWidth(UnitValue.createPercentValue(100));
            infoTable.setMarginBottom(15);

            // Cột trái: Thông tin hóa đơn
            Cell leftInfo = new Cell().setBorder(Border.NO_BORDER);
            leftInfo.add(new Paragraph("Mã hóa đơn: #" + bill.getBillId().toString().substring(0, 8).toUpperCase()).setBold());
            leftInfo.add(new Paragraph("Ngày lập: " + bill.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
            leftInfo.add(new Paragraph("Thu ngân: " + bill.getUsers().getFullName()));

            // Cột phải: Thông tin bệnh nhân
            Cell rightInfo = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
            rightInfo.add(new Paragraph("Khách hàng: " + bill.getMedicalRecord().getPatientName()).setBold());
            String phone = bill.getMedicalRecord().getPatient().getPhone();
            rightInfo.add(new Paragraph("SĐT: " + (phone != null ? phone : "Trống")));

            infoTable.addCell(leftInfo);
            infoTable.addCell(rightInfo);
            document.add(infoTable);

            // 5. Bảng dịch vụ
            // Cột: STT, Tên dịch vụ, SL, Đơn giá, Thành tiền
            float[] columnWidths = {5, 45, 10, 20, 20};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            // Header Bảng
            String[] headers = {"STT", "Tên dịch vụ", "SL", "Đơn giá", "Thành tiền"};
            for (String header : headers) {
                table.addHeaderCell(new Cell().add(new Paragraph(header).setBold().setFontSize(10))
                        .setBackgroundColor(new DeviceRgb(240, 240, 240))
                        .setTextAlignment(TextAlignment.CENTER));
            }

            // Body Bảng
            int index = 1;
            for (BillingDetail item : bill.getDetails()) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(index++)).setFontSize(10)).setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(item.getService().getName()).setFontSize(10)));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(item.getQuantity())).setFontSize(10)).setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(formatCurrency(item.getUnitPrice())).setFontSize(10)).setTextAlignment(TextAlignment.RIGHT));
                table.addCell(new Cell().add(new Paragraph(formatCurrency(item.getSubTotal())).setFontSize(10)).setTextAlignment(TextAlignment.RIGHT));
            }
            document.add(table);

            // 6. Tổng tiền
            Table totalTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}));
            totalTable.setWidth(UnitValue.createPercentValue(100));
            totalTable.setMarginTop(10);

            totalTable.addCell(new Cell().add(new Paragraph("Tổng tiền:").setBold().setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
            totalTable.addCell(new Cell().add(new Paragraph(formatCurrency(bill.getTotalAmount())).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));

            totalTable.addCell(new Cell().add(new Paragraph("Đã thanh toán:").setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
            totalTable.addCell(new Cell().add(new Paragraph(formatCurrency(bill.getAmountPaid())).setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));

            BigDecimal remaining = bill.getFinalAmount().subtract(bill.getAmountPaid());
            if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;

            totalTable.addCell(new Cell().add(new Paragraph("Còn lại:").setBold().setTextAlignment(TextAlignment.RIGHT)).setBorder(Border.NO_BORDER));
            totalTable.addCell(new Cell().add(new Paragraph(formatCurrency(remaining)).setBold().setTextAlignment(TextAlignment.RIGHT).setFontColor(new DeviceRgb(255, 0, 0))).setBorder(Border.NO_BORDER));

            document.add(totalTable);

            // 7. Footer
            document.add(new Paragraph("\n\n"));
            Paragraph footer = new Paragraph("Cảm ơn quý khách đã sử dụng dịch vụ!")
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo PDF hóa đơn: " + e.getMessage(), e);
        }
    }
} 
