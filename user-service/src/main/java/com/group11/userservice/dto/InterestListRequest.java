package com.group11.userservice.dto;

import java.util.List;

public record InterestListRequest(
        String userId,
        List<String> doctorIds) {
}
