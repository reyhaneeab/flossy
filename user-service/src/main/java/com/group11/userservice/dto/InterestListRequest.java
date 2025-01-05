package com.group11.userservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InterestListRequest(
                String userId,
                List<String> doctors) {
}
