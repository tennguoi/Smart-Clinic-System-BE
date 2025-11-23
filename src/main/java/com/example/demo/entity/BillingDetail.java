// src/main/java/com/example/demo/entity/BillingDetail.java
package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "BILLING_DETAILS")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BillingDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "detail_id")
    private UUID detailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private BillingRecord billingRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(length = 500)
    private String description;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "sub_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal subTotal = BigDecimal.ZERO;
}