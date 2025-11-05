package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class UserAdminResponse {
    private UUID userId;
    private String email;
    private String fullName;
    private List<String> roles;
}
