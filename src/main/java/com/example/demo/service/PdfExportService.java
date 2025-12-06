package com.example.demo.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.example.demo.entity.BillingDetail;
import com.example.demo.entity.BillingRecord;
import com.example.demo.entity.MedicalRecord;
import com.example.demo.entity.MedicalRecordServiceEntity;
import com.example.demo.entity.Patient;
import com.example.demo.entity.Prescription;
import com.example.demo.repository.MedicalRecordServiceRepository;
import com.example.demo.repository.PrescriptionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

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

    // ==========================
    // HEADER CHUNG PHÒNG KHÁM
    // ==========================
    private void addClinicHeader(Document document, com.example.demo.entity.Clinic clinic) {
        // Tên phòng khám căn giữa
        if (clinic != null && clinic.getName() != null && !clinic.getName().isBlank()) {
            document.add(new Paragraph(clinic.getName())
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10));
        }

        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{20, 80}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20)
                .setBorder(Border.NO_BORDER);

        // ===== LOGO =====
        Cell logoCell = new Cell().setBorder(Border.NO_BORDER).setPadding(5);
        boolean loaded = false;

        try {
            String logoPath = clinic != null ? clinic.getLogoUrl() : null;

            if (logoPath != null && !logoPath.isBlank()) {
                // 1. Thử load từ URL (http/https)
                if (logoPath.startsWith("http://") || logoPath.startsWith("https://")) {
                    try {
                        Image logo = new Image(ImageDataFactory.create(new java.net.URL(logoPath)));
                        logo.setWidth(80).setHeight(80);
                        logoCell.add(logo);
                        loaded = true;
                        logger.info("✅ Logo loaded from URL: {}", logoPath);
                    } catch (Exception urlEx) {
                        logger.debug("Failed to load logo from URL {}: {}", logoPath, urlEx.getMessage());
                    }
                }
                
                // 2. Thử load từ resource stream (static folder)
                if (!loaded) {
                    String normalized = logoPath.startsWith("/") ? logoPath : "/" + logoPath;
                    java.io.InputStream is = getClass().getResourceAsStream(normalized);
                    
                    if (is != null) {
                        try {
                            byte[] bytes = is.readAllBytes();
                            if (bytes.length > 0) {
                                Image logo = new Image(ImageDataFactory.create(bytes));
                                logo.setWidth(80).setHeight(80);
                                logoCell.add(logo);
                                loaded = true;
                                logger.info("✅ Logo loaded from resource: {}", normalized);
                            }
                            is.close();
                        } catch (Exception resEx) {
                            logger.debug("Failed to load logo from resource {}: {}", normalized, resEx.getMessage());
                        }
                    }
                }
                
                // 3. Thử load từ file system (uploads folder)
                if (!loaded) {
                    try {
                        // Xử lý path: bỏ "/" ở đầu nếu có
                        String filePath = logoPath.startsWith("/") ? logoPath.substring(1) : logoPath;
                        
                        java.io.File logoFile = new java.io.File(filePath);
                        if (logoFile.exists() && logoFile.isFile()) {
                            Image logo = new Image(ImageDataFactory.create(logoFile.getAbsolutePath()));
                            logo.setWidth(80).setHeight(80);
                            logoCell.add(logo);
                            loaded = true;
                            logger.info("✅ Logo loaded from file system: {}", logoFile.getAbsolutePath());
                        } else {
                            logger.debug("❌ Logo file does not exist: {} (absolute: {})", 
                                filePath, logoFile.getAbsolutePath());
                        }
                    } catch (Exception fileEx) {
                        logger.debug("Failed to load logo from file system {}: {}", logoPath, fileEx.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error loading logo: {}", e.getMessage());
        }

        if (!loaded) {
            logger.warn("⚠️ Logo not found. Using placeholder. Logo path: {}", 
                clinic != null ? clinic.getLogoUrl() : "null");
            logoCell.add(new Paragraph("LOGO")
                    .setBold()
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(30));
        }

        headerTable.addCell(logoCell);

        // ===== THÔNG TIN PHÒNG KHÁM =====
        Cell infoCell = new Cell().setBorder(Border.NO_BORDER).setPadding(5);

        if (clinic != null) {
            if (clinic.getAddress() != null && !clinic.getAddress().isBlank()) {
                infoCell.add(new Paragraph("Địa chỉ: " + clinic.getAddress()).setFontSize(12));
            }

            String contact = "";
            if (clinic.getPhone() != null && !clinic.getPhone().isBlank()) {
                contact += "SĐT: " + clinic.getPhone();
            }
            if (clinic.getEmail() != null && !clinic.getEmail().isBlank()) {
                if (!contact.isEmpty()) contact += " – ";
                contact += "Email: " + clinic.getEmail();
            }
            if (!contact.isEmpty()) {
                infoCell.add(new Paragraph(contact).setFontSize(12));
            }

            if (clinic.getWebsite() != null && !clinic.getWebsite().isBlank()) {
                infoCell.add(new Paragraph("Website: " + clinic.getWebsite())
                        .setFontSize(12));
            }

            String working = "";
            if (clinic.getMorningStartTime() != null && clinic.getMorningEndTime() != null) {
                working += "Sáng: " + clinic.getMorningStartTime() + " - " + clinic.getMorningEndTime();
            }
            if (clinic.getAfternoonStartTime() != null && clinic.getAfternoonEndTime() != null) {
                if (!working.isEmpty()) working += " | ";
                working += "Chiều: " + clinic.getAfternoonStartTime() + " - " + clinic.getAfternoonEndTime();
            }
            if (!working.isEmpty()) {
                infoCell.add(new Paragraph("Giờ làm việc: " + working).setFontSize(12));
            }
        }

        headerTable.addCell(infoCell);
        document.add(headerTable);
    }


    // ==========================
    // PHIẾU KHÁM BỆNH
    // ==========================
    public byte[] generateMedicalRecordPdf(MedicalRecord record) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            document.setMargins(40, 40, 40, 40);

            // FONT hỗ trợ tiếng Việt
            try {
                PdfFont font = null;
                try (java.io.InputStream is = getClass().getResourceAsStream("/static/NotoSans-Regular.ttf")) {
                    if (is != null) {
                        byte[] bytes = is.readAllBytes();
                        font = PdfFontFactory.createFont(bytes,
                                PdfEncodings.IDENTITY_H,
                                PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                    }
                }
                if (font == null) {
                    font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                }
                document.setFont(font);
            } catch (Exception ignored) {}

            com.example.demo.entity.Clinic clinic = clinicService.getClinicEntity();

            // HEADER
            addClinicHeader(document, clinic);

            // TITLE
            document.add(new Paragraph("PHIẾU KHÁM BỆNH")
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // THÔNG TIN CHUNG
            String visitDate = record.getCreatedAt() != null
                    ? record.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : "Trống";

            String ticketNumber = "PK-" + record.getRecordId().toString().substring(0, 8).toUpperCase();

            String doctorName = record.getDoctor() != null
                    ? record.getDoctor().getFullName()
                    : "Trống";

            document.add(new Paragraph("Ngày khám: " + visitDate
                    + "     Số phiếu: " + ticketNumber
                    + "     Bác sĩ khám: BS. " + doctorName)
                    .setFontSize(12)
                    .setMarginBottom(15));

            // THÔNG TIN BỆNH NHÂN
            Patient patient = record.getPatient();

            String patientName = record.getPatientName();
            if (patientName == null || patientName.isBlank()) {
                patientName = patient != null ? patient.getFullName() : "Trống";
            }

            String patientPhone = (patient != null && patient.getPhone() != null && !patient.getPhone().isBlank())
                    ? patient.getPhone()
                    : "Trống";

            String patientAddress = (patient != null && patient.getAddress() != null && !patient.getAddress().isBlank())
                    ? patient.getAddress()
                    : "Trống";

            LocalDate dob = patient != null ? patient.getDob() : null;
            String dobStr = dob != null
                    ? dob.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : "Trống";

            String gender = "Trống";
            if (patient != null && patient.getGender() != null) {
                gender = patient.getGender().getLabel();
            }

            document.add(new Paragraph("I. THÔNG TIN BỆNH NHÂN")
                    .setBold()
                    .setMarginBottom(8));
            document.add(createDottedLine("Họ tên:", patientName, 12));
            document.add(createDottedLine("Giới tính:", gender, 12));
            document.add(createDottedLine("Ngày sinh:", dobStr, 12));
            document.add(createDottedLine("SĐT:", patientPhone, 12));
            document.add(createDottedLine("Địa chỉ:", patientAddress, 12));

            // TRIỆU CHỨNG
            document.add(new Paragraph("II. LÝ DO KHÁM / TRIỆU CHỨNG")
                    .setBold()
                    .setMarginTop(15)
                    .setMarginBottom(8));

            String symptoms = (patient != null && patient.getNotes() != null && !patient.getNotes().isBlank())
                    ? patient.getNotes()
                    : null;
            document.add(createDottedLine("Triệu chứng:", symptoms, 12));

            // CHẨN ĐOÁN
            document.add(new Paragraph("III. CHẨN ĐOÁN")
                    .setBold()
                    .setMarginTop(15)
                    .setMarginBottom(8));

            document.add(createDottedLine("Chẩn đoán:", record.getDiagnosis(), 12));

            // DỊCH VỤ
            document.add(new Paragraph("IV. CHỈ ĐỊNH DỊCH VỤ")
                    .setBold()
                    .setMarginTop(15)
                    .setMarginBottom(8));

            Table serviceTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(15);

            Cell h1 = new Cell().add(new Paragraph("Dịch vụ").setBold())
                    .setBorder(new SolidBorder(0.5f));
            Cell h2 = new Cell().add(new Paragraph("Giá").setBold())
                    .setBorder(new SolidBorder(0.5f));

            serviceTable.addCell(h1);
            serviceTable.addCell(h2);

            List<MedicalRecordServiceEntity> serviceEntities =
                    medicalRecordServiceRepository.findAllByMedicalRecord_RecordId(record.getRecordId());

            if (serviceEntities.isEmpty()) {
                Cell e1 = new Cell().add(new Paragraph("Trống")).setBorder(new SolidBorder(0.5f));
                Cell e2 = new Cell().add(new Paragraph("Trống")).setBorder(new SolidBorder(0.5f));
                serviceTable.addCell(e1);
                serviceTable.addCell(e2);
            } else {
                for (MedicalRecordServiceEntity s : serviceEntities) {
                    String name = (s.getService() != null && s.getService().getName() != null)
                            ? s.getService().getName()
                            : "Dịch vụ";
                    int quantity = (s.getQuantity() != null) ? s.getQuantity() : 1;
                    BigDecimal price = (s.getUnitPrice() != null) ? s.getUnitPrice() : BigDecimal.ZERO;

                    String text = name + (quantity > 1 ? " x" + quantity : "");
                    if (s.getNote() != null && !s.getNote().isBlank()) {
                        text += "\nGhi chú: " + s.getNote();
                    }

                    serviceTable.addCell(new Cell()
                            .add(new Paragraph(text))
                            .setBorder(new SolidBorder(0.5f)));

                    BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));
                    serviceTable.addCell(new Cell()
                            .add(new Paragraph(formatCurrency(total)))
                            .setBorder(new SolidBorder(0.5f)));
                }
            }

            document.add(serviceTable);

            // ĐƠN THUỐC TÓM TẮT
            document.add(new Paragraph("V. ĐƠN THUỐC")
                    .setBold()
                    .setMarginTop(15)
                    .setMarginBottom(8));

            Table presTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .setWidth(UnitValue.createPercentValue(100));

            presTable.addCell(new Cell()
                    .add(new Paragraph("Thuốc").setBold())
                    .setBorder(new SolidBorder(0.5f)));
            presTable.addCell(new Cell()
                    .add(new Paragraph("Hướng dẫn sử dụng").setBold())
                    .setBorder(new SolidBorder(0.5f)));

            Optional<Prescription> prescriptionOpt = prescriptionRepository.findByRecordId(record.getRecordId());
            if (prescriptionOpt.isPresent()) {
                Prescription rx = prescriptionOpt.get();
                String drugs = rx.getDrugs() != null ? rx.getDrugs() : "";
                String instructions = rx.getInstructions() != null ? rx.getInstructions() : "";

                presTable.addCell(new Cell()
                        .add(new Paragraph(drugs))
                        .setBorder(new SolidBorder(0.5f)));
                presTable.addCell(new Cell()
                        .add(new Paragraph(instructions))
                        .setBorder(new SolidBorder(0.5f)));
            } else {
                presTable.addCell(new Cell()
                        .add(new Paragraph(""))
                        .setBorder(new SolidBorder(0.5f)));
                presTable.addCell(new Cell()
                        .add(new Paragraph(""))
                        .setBorder(new SolidBorder(0.5f)));
            }

            document.add(presTable);

            // CHỮ KÝ BÁC SĨ
            document.add(new Paragraph("\n\n"));
            document.add(new Paragraph("Bác sĩ khám bệnh")
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.RIGHT));
            document.add(new Paragraph("(Ký và ghi rõ họ tên)")
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(40));
            document.add(new Paragraph(".......................................................")
                    .setTextAlignment(TextAlignment.RIGHT));

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo PDF phiếu khám: " + e.getMessage(), e);
        }
    }

    // ==========================
    // HÓA ĐƠN THANH TOÁN
    // ==========================
    public byte[] generateInvoicePdf(BillingRecord bill) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            document.setMargins(40, 40, 40, 40);

            // FONT
            try {
                PdfFont font = null;
                try (java.io.InputStream is = getClass().getResourceAsStream("/static/NotoSans-Regular.ttf")) {
                    if (is != null) {
                        byte[] bytes = is.readAllBytes();
                        font = PdfFontFactory.createFont(bytes,
                                PdfEncodings.IDENTITY_H,
                                PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                    }
                }
                if (font == null) {
                    font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                }
                document.setFont(font);
            } catch (Exception ignored) {}

            com.example.demo.entity.Clinic clinic = clinicService.getClinicEntity();

            // HEADER
            addClinicHeader(document, clinic);

            // TITLE
            document.add(new Paragraph("HÓA ĐƠN THANH TOÁN")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15));

            // THÔNG TIN HÓA ĐƠN
            Table info = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(15);

            Cell left = new Cell().setBorder(Border.NO_BORDER);
            left.add(new Paragraph("Mã hóa đơn: #" + bill.getBillId().toString().substring(0, 8))
                    .setBold());
            left.add(new Paragraph("Ngày lập: " + bill.getCreatedAt()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
            left.add(new Paragraph("Thu ngân: " + bill.getUsers().getFullName()));

            Cell right = new Cell()
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT);
            right.add(new Paragraph("Khách hàng: " + bill.getMedicalRecord().getPatientName())
                    .setBold());
            String phone = bill.getMedicalRecord().getPatient() != null
                    ? bill.getMedicalRecord().getPatient().getPhone()
                    : null;
            right.add(new Paragraph("SĐT: " + (phone != null ? phone : "Trống")));

            info.addCell(left);
            info.addCell(right);
            document.add(info);

            // BẢNG DỊCH VỤ
            float[] widths = {5, 45, 10, 20, 20};
            Table table = new Table(UnitValue.createPercentArray(widths))
                    .setWidth(UnitValue.createPercentValue(100));

            String[] headers = {"STT", "Tên dịch vụ", "SL", "Đơn giá", "Thành tiền"};
            for (String h : headers) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setBold().setFontSize(10))
                        .setBackgroundColor(new DeviceRgb(240, 240, 240))
                        .setTextAlignment(TextAlignment.CENTER));
            }

            int index = 1;
            for (BillingDetail item : bill.getDetails()) {
                table.addCell(new Cell()
                        .add(new Paragraph(String.valueOf(index++)).setFontSize(10))
                        .setTextAlignment(TextAlignment.CENTER));

                table.addCell(new Cell()
                        .add(new Paragraph(item.getService().getName()).setFontSize(10)));

                table.addCell(new Cell()
                        .add(new Paragraph(String.valueOf(item.getQuantity())).setFontSize(10))
                        .setTextAlignment(TextAlignment.CENTER));

                table.addCell(new Cell()
                        .add(new Paragraph(formatCurrency(item.getUnitPrice())))
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.RIGHT));

                table.addCell(new Cell()
                        .add(new Paragraph(formatCurrency(item.getSubTotal())))
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.RIGHT));
            }

            document.add(table);

            // TỔNG TIỀN
            BigDecimal remaining = bill.getFinalAmount().subtract(bill.getAmountPaid());
            if (remaining.compareTo(BigDecimal.ZERO) < 0) {
                remaining = BigDecimal.ZERO;
            }

            Table totalTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(10);

            totalTable.addCell(cellRight("Tổng tiền:", true));
            totalTable.addCell(cellRight(formatCurrency(bill.getTotalAmount()), false));

            totalTable.addCell(cellRight("Đã thanh toán:", false));
            totalTable.addCell(cellRight(formatCurrency(bill.getAmountPaid()), false));

            totalTable.addCell(cellRight("Còn lại:", true));
            totalTable.addCell(new Cell()
                    .add(new Paragraph(formatCurrency(remaining))
                            .setBold()
                            .setFontColor(new DeviceRgb(255, 0, 0))
                            .setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER));

            document.add(totalTable);

            // FOOTER
            document.add(new Paragraph("\n\n"));
            document.add(new Paragraph("Cảm ơn quý khách đã sử dụng dịch vụ!")
                    .setItalic()
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo PDF hóa đơn: " + e.getMessage(), e);
        }
    }

    // ==========================
    // HELPERS
    // ==========================
    private Paragraph createDottedLine(String label, String value, float fontSize) {
        String safeLabel = label != null ? label.trim() : "";
        boolean hasValue = value != null && !value.trim().isEmpty();

        StringBuilder sb = new StringBuilder();
        if (!safeLabel.isEmpty()) {
            sb.append(safeLabel).append(" ");
        }
        sb.append(hasValue ? value.trim() : "Trống");

        return new Paragraph(sb.toString())
                .setFontSize(fontSize)
                .setMarginBottom(5);
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }
        try {
            return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
        } catch (Exception e) {
            return amount.toPlainString();
        }
    }

    private Cell cellRight(String text, boolean bold) {
        Paragraph p = new Paragraph(text)
                .setTextAlignment(TextAlignment.RIGHT);
        if (bold) {
            p.setBold();
        }
        return new Cell()
                .add(p)
                .setBorder(Border.NO_BORDER);
    }
}
