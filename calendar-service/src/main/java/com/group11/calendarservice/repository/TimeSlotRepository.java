package com.group11.calendarservice.repository;

import com.group11.calendarservice.model.TimeSlot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimeSlotRepository extends MongoRepository<TimeSlot, String> {

    // Create a query to find overlapping time slots by using mangoDB query language
    @Query("{ 'dentistName': ?0, 'startTime': { $lt: ?2 }, 'endTime': { $gt: ?1 } }")
    List<TimeSlot> findOverlappingSlots(String dentistName, LocalDateTime startTime, LocalDateTime endTime);
}