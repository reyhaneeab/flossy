package com.group11.dentistservice.service;

import com.group11.dentistservice.dto.DentistRequest;
import com.group11.dentistservice.dto.DentistResponse;
import com.group11.dentistservice.model.Dentist;
import com.group11.dentistservice.repository.DentistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DentistService {
    private final DentistRepository dentistRepository;


    public DentistResponse createDentist(DentistRequest dentistRequest) {
        Dentist dentist = Dentist.builder()
                .officeName(dentistRequest.officeName())
                .officeEmail(dentistRequest.officeEmail())
                .password(dentistRequest.password())
                .phone(dentistRequest.phone())
                .address(dentistRequest.address())
                .build();
        dentistRepository.save(dentist);
        log.info("Dentist Office {} has been registered successfully", dentist.getOfficeName());
        return new DentistResponse(dentist.getId(), dentist.getOfficeName(), dentist.getOfficeEmail(), dentist.getPassword(), dentist.getPhone(), dentist.getAddress());
    }

}