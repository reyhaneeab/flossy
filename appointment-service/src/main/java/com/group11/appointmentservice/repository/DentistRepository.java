package com.group11.appointmentservice.repository;

import com.group11.model.Dentist;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DentistRepository extends MongoRepository<Dentist, String>  {
}
