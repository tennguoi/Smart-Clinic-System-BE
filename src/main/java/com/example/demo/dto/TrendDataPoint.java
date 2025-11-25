// src/main/java/com/example/demo/dto/TrendDataPoint.java
package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrendDataPoint {
    private String date;   // Format: dd/MM
    private Number value;  // Count or Revenue
    private String type;   // Optional: "New", "Returning"...

    public TrendDataPoint(String date, Long value, String type) {
        this.date = date;
        this.value = value;
        this.type = type;
    }

    public TrendDataPoint(String date, BigDecimal value, String type) {
        this.date = date;
        this.value = value;
        this.type = type;
    }

    public TrendDataPoint(Object date, Object value, Object type) {
        this.date = date != null ? date.toString() : "";
        if (value instanceof Number n) {
            this.value = n;
        } else {
            this.value = 0;
        }
        this.type = type != null ? type.toString() : "";
    }
}
