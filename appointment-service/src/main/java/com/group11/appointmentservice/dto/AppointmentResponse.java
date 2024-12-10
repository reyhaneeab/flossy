package com.group11.appointmentservice.dto;

import com.group11.model.TimeSlot;
import com.group11.model.User;

public record AppointmentResponse(
    boolean success,
    String message,
    User user,
    TimeSlot timeSlot
) {}