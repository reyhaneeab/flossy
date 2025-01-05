package com.group11.userservice.dto;

import java.util.List;

public record InterestListResponse(
        String userId,
        List<String> doctorIds) {
}
