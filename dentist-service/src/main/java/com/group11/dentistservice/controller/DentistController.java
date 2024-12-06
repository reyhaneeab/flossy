
package com.group11.dentistservice.controller;

import com.group11.dentistservice.service.DentistService;
import com.group11.model.Dentist;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dentists")
@RequiredArgsConstructor
public class DentistController {
    private final DentistService dentistService;

    @GetMapping
    public List<Dentist> getAllDentists() {
        return dentistService.getAllDentists();
    }
}