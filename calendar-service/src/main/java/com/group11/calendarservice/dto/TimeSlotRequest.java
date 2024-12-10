package com.group11.calendarservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TimeSlotRequest(
        String dentistName,
        LocalDate date,
        LocalDateTime startTime,
        LocalDateTime endTime

) {}