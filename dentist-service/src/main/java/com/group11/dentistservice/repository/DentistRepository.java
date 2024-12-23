package com.group11.dentistservice.repository;

import com.group11.model.Dentist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface DentistRepository extends MongoRepository<Dentist, String> {
    Optional<Dentist> findByEmail(String email);

    @Transactional
    @Query("{ '_id': ?0 }, { $addToSet: { 'interestList': { $each: ?1 } } }")
    void addDoctorsToPatientInterestList(String patientId, List<String> doctorIds);

    @Query(value = "{ '_id': ?0 }", fields = "{ 'interestList': 1 }")
    List<String> getInterestListByPatientId(String patientId);
}