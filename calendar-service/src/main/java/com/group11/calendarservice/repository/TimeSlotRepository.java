package com.group11.calendarservice.repository;

import com.group11.calendarservice.model.TimeSlot;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TimeSlotRepository extends MongoRepository<TimeSlot, String> {
}
