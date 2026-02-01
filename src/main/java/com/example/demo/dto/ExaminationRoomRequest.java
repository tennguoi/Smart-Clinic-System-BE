package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExaminationRoomRequest {

    @NotBlank(message = "Tên phòng khám không được để trống")
    private String roomName;

    private UUID doctorId;

    private UUID currentQueueId;

    @NotNull(message = "Trạng thái không được để trống")
    private String status; // "Available" hoặc "Occupied"

    private Boolean isActive;
}

