package com.group11.calendarservice.repository;

import com.group11.calendarservice.model.Calendar;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CalendarRepository extends MongoRepository<Calendar, String> {
    Calendar findByDate(String date);
}
