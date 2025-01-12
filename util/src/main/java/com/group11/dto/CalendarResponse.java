package com.group11.dto;

import com.group11.model.Dentist;

import java.util.List;

public record CalendarResponse(
        String id,
        Dentist owner,
        int weekNumber,
        int year,
        List<TimeSlotResponse> timeSlots
) {}