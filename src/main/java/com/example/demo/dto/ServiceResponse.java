
package com.example.demo.dto;

import com.example.demo.entity.Service.ServiceCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse {


    private String name;
    private String description;
    private ServiceCategory category;
    private BigDecimal price;
}
