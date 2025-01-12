package com.group11.dto;
import lombok.Data;

@Data
public class AppointmentRequest {
    private String timeSlotId;
    private String userId;
}

