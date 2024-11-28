package com.group11.calendarservice.repository;

import com.group11.calendarservice.model.Calendar;
import com.mongodb.RequestContext;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarRepository extends MongoRepository<Calendar, String> {
    List<Calendar> findByDentistName(String dentistName);
}

