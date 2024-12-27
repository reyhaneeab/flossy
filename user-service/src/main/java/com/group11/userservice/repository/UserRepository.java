package com.group11.userservice.repository;

import com.group11.model.User;
import com.group11.model.TimeSlot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    void deleteByEmail(String email);

    @Query("{ '_id': ?0 }")
    void addTimeSlotsToUserInterestList(String userId, List<String> timeSlotIds);

    @Query(value = "{ '_id': ?0 }", fields = "{ 'interestList': 1 }")
    List<String> getInterestListByUserId(String userId);

    Optional<TimeSlot> findTimeSlotById(String id);
}
