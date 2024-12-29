package com.group11.dto;

import java.util.List;

public record InterestListRequest(
        String userId,
        List<String> timeSlotIds) {
}
