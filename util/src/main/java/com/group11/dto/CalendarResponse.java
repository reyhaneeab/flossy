package com.group11.dto;

import java.util.List;

public record CalendarResponse(
        String id,
        String ownerId,
        int weekNumber,
        int year,
        List<TimeSlotResponse> timeSlots
) {}