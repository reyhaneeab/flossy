package com.group11.dentistservice.dto;

public record DentistResponse(String id,
                              String officeName,
                              String email,
                              String password,
                              String phone,
                              String address
                              ) {}
