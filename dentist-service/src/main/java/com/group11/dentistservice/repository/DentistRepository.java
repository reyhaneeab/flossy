package com.group11.dentistservice.repository;

import com.group11.model.Dentist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public interface DentistRepository extends MongoRepository<Dentist, String> {
    Logger log = LoggerFactory.getLogger(DentistRepository.class);

    Optional<Dentist> findByEmail(String email);

    default Dentist saveWithLogging(Dentist dentist) {
        Dentist savedDentist = save(dentist);
        log.info("Dentist office {} has been saved to MongoDB with id {}", savedDentist.getOfficeName(), savedDentist.getId());
        return savedDentist;
    }
}