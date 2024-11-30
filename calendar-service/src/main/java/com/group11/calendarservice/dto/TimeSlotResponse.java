package com.group11.calendarservice.dto;

import com.group11.model.Dentist;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TimeSlotResponse(
                String id,
                LocalDate date,
                LocalDateTime startTime,
                LocalDateTime endTime,
                Dentist owner
                ) {
}