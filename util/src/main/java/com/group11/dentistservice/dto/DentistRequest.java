
package com.group11.dentistservice.dto;

public record DentistRequest(
        String officeName,
        String email,
        String password,
        String address,
        String phone,
        double latitude,
        double longitude) {
}