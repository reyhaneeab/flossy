package com.group11.appointmentservice.dto;
import lombok.Data;

@Data
public class BookAppointmentRequest {
    private String timeSlotId;
    private String userId;
}

