package com.group11.calendarservice.repository;

import com.group11.model.Calendar;
import com.group11.model.Dentist;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CalendarRepository extends MongoRepository<Calendar, String> {
    Calendar findByOwnerAndWeekNumber(Dentist owner, int weekNumber);
}