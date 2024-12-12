package com.group11.dentistservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DentistResponse(
                String officeName,
                String email,
                String password,
                String address,
                String phone,
                double latitude,
                double longitude,
                List<String> doctors) {
}