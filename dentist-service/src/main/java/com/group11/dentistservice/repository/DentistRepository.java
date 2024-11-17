package com.group11.dentistservice.repository;

import com.group11.dentistservice.model.Dentist;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DentistRepository extends MongoRepository<Dentist, String>  {
}
