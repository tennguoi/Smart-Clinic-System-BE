package com.example.demo.enums;

public enum RoleName {

    ADMIN("admin", "Quản trị viên"),
    BAC_SI("bac_si", "Bác sĩ"),
    TIEP_TAN("tiep_tan", "Lễ tân");

    private final String dbName;     // chính là giá trị lưu trong cột name của bảng roles
    private final String label;      // tên hiển thị tiếng Việt

    RoleName(String dbName, String label) {
        this.dbName = dbName;
        this.label = label;
    }

    public String getDbName() {
        return dbName;
    }

    public String getLabel() {
        return label;
    }

    // Rất tiện: trả về authority chuẩn Spring Security
    public String getAuthority() {
        return "ROLE_" + dbName.toUpperCase();
    }

    // Tìm enum từ giá trị trong DB (rất hay dùng)
    public static RoleName fromDbName(String dbName) {
        for (RoleName role : values()) {
            if (role.dbName.equals(dbName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Không tồn tại RoleName với dbName = " + dbName);
    }
}