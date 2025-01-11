package com.group11.userservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.group11.model.TimeSlot;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InterestListRequest(
        String userId,
        List<String> doctors,
        List<TimeSlot> timeSlots) {
}
