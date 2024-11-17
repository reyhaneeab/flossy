package com.group11.dentistservice.controller;

import com.group11.dentistservice.dto.DentistResponse;
import com.group11.dentistservice.dto.DentistRequest;
import com.group11.dentistservice.service.DentistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dentist")
@RequiredArgsConstructor
public class DentistController {

    private final DentistService dentistService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DentistResponse createDentist(@RequestBody DentistRequest dentistRequest){
        return dentistService.createDentist(dentistRequest);
    }

}