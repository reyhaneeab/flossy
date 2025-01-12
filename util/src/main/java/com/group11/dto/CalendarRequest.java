package com.group11.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CalendarRequest(
        int year,
        int weekNumber,
        List<TimeSlotRequest> timeSlots,
        String ownerId
) {}

