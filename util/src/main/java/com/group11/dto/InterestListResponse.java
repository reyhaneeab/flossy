package com.group11.dto;

import java.util.List;

public record InterestListResponse(
        String userId,
        List<String> timeSlotIds) {
}
