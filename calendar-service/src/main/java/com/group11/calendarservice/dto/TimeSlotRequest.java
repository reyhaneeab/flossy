package com.group11.calendarservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TimeSlotRequest(List<TimeSlotDto> timeSlots) {

    public static record TimeSlotDto(String dentistName, LocalDateTime startTime, LocalDateTime endTime) {
    }
}