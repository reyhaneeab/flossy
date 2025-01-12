package com.group11.userservice.dto;

import com.group11.model.Dentist;

import java.util.List;

public record InterestListResponse(
        String userId,
        List<Dentist> dentists) {
}
