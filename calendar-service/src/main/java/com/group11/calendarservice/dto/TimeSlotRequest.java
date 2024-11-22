package com.group11.calendarservice.dto;

import java.time.LocalDateTime;

public record TimeSlotRequest(String dentistName,
                LocalDateTime startTime,
                LocalDateTime endTime) {
}
