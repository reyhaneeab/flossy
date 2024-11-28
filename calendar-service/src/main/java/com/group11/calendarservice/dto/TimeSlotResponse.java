package com.group11.calendarservice.dto;

import java.time.LocalDateTime;

public record TimeSlotResponse(
                String id,
                String dentistName,
                LocalDateTime startTime,
                LocalDateTime endTime,
                String message) {
}