package com.group11.calendarservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
// Encapsulates the request for creating a new calendar
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CalendarRequest(
        int year,
        int weekNumber,
        List<TimeSlotRequest> timeSlots,
        String ownerId
) {}

