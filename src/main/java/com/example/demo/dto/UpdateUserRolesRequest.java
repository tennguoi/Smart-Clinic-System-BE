package com.example.demo.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRolesRequest {
    @NotEmpty
    private List<String> roles; // new role names to replace current ones
}
