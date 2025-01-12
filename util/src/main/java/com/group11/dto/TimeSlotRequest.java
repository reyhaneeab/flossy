package com.group11.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TimeSlotRequest(
        LocalDate date,
        LocalDateTime startTime,
        LocalDateTime endTime
)
{}