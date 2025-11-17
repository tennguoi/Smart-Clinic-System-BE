package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExaminationRoomResponse {

    private UUID roomId;
    private String roomName;
    private UUID doctorId;
    private String doctorName;
    private UUID currentQueueId;
    private String status;
    private Boolean isActive;
}

