package com.example.demo.dto.appointment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppointmentChatbotResponse {

    private boolean success;
    private String message;

    private UUID appointmentId;
    private String appointmentCode;
    private String patientName;
    private String phone;
    private String appointmentTime;
    private String status;
    private String notes;
}