package com.example.demo.enums;

public enum DoctorStatsRange {
    DAY,
    WEEK,
    MONTH;

    public static DoctorStatsRange from(String raw) {
        if (raw == null || raw.isBlank()) {
            return DAY;
        }
        try {
            return DoctorStatsRange.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("range must be one of: day, week, month");
        }
    }
}

