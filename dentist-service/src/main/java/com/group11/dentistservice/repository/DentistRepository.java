package com.group11.dentistservice.repository;

import com.group11.model.Dentist;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface DentistRepository extends MongoRepository<Dentist, String>  {
    Optional<Dentist> findByEmail(String email);
}