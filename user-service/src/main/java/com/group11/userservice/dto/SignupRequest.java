package com.group11.userservice.dto;

public record SignupRequest(
        String email,
        String firstName,
        String lastName,
        String phone,

        String password
) {}
