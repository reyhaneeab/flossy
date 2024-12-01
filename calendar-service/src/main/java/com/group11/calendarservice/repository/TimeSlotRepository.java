package com.group11.calendarservice.repository;

import com.group11.model.TimeSlot;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TimeSlotRepository extends MongoRepository<TimeSlot, String> {
    @Query("{'startTime': {$lt: ?1}, 'endTime': {$gt: ?0}}")
    List<TimeSlot> findOverlappingSlots(LocalDateTime start, LocalDateTime end);
}