package com.group11.userservice.dto;

import java.util.List;

import com.group11.model.TimeSlot;

public record InterestListResponse(
                String userId,
                List<TimeSlot> timeSlots) {
}
