package com.group11.dto;

public record UserRequest (
        String email,
        String firstName,
        String lastName,
        String phone,
        String password
) {}
