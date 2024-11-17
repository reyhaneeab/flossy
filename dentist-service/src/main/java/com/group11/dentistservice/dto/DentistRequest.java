package com.group11.dentistservice.dto;

public record DentistRequest(String id,
                             String officeName,
                             String officeEmail,
                             String password,
                             String phone,
                             String address) {
}
