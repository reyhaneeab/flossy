package com.group11.appointmentservice.repository;

import com.group11.model.TimeSlot;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TimeSlotRepository extends MongoRepository<TimeSlot, String> {}