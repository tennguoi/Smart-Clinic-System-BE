package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignRoomRequest {

    @NotNull(message = "ID hàng đợi (queueId) không được để trống")
    private UUID queueId;

    @NotNull(message = "ID phòng khám (roomId) không được để trống")
    private UUID roomId;
}