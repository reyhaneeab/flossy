package com.group11.userservice.dto;

import com.group11.model.TimeSlot;

import java.util.List;

public record InterestListResponse(
        String userId,
        List<String> doctors,
        List<TimeSlot> timeSlots) {
}
