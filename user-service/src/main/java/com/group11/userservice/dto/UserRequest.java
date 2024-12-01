package com.group11.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserRequest(
        String email,
        String firstName,
        String lastName,
        String phone,
        String password
) {}
