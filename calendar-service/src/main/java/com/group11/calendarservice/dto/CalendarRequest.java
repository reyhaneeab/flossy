package com.group11.calendarservice.dto;

import java.time.LocalDate;
import java.util.List;

public record CalendarRequest(
        String id,
        String ownerId,
        int weekNumber,
        int year,
        List<TimeSlotRequest> timeSlots
) {}

