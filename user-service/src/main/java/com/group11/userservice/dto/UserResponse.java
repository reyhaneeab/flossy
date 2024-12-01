package com.group11.userservice.dto;

public record UserResponse (
    String id,
    String firstName,
    String lastName,
    String email,
    String phone
) {}
