package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrendDataPoint {
    private String date; // Format: dd/MM
    private Number value; // Count or Revenue
    private String type; // Optional: "New", "Returning" for patient trends

    public TrendDataPoint(String date, Long value, String type) {
        this.date = date;
        this.value = value;
        this.type = type;
    }

    public TrendDataPoint(String date, java.math.BigDecimal value, String type) {
        this.date = date;
        this.value = value;
        this.type = type;
    }

    // Generic constructor to handle potential type mismatches (e.g. if DATE_FORMAT returns Object)
    public TrendDataPoint(Object date, Object value, Object type) {
        this.date = date != null ? date.toString() : "";
        if (value instanceof Number) {
            this.value = (Number) value;
        } else {
            this.value = 0;
        }
        this.type = type != null ? type.toString() : "";
    }
}
