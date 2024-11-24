package com.group11.calendarservice.controller;

import com.group11.calendarservice.dto.TimeSlotRequest;
import com.group11.calendarservice.dto.TimeSlotResponse;
import com.group11.calendarservice.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timeslots")
@RequiredArgsConstructor
public class TimeSlotController {

    private final CalendarService calendarService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<TimeSlotResponse> createTimeSlots(@RequestBody TimeSlotRequest timeSlotRequest) {
        return calendarService.createTimeSlots(timeSlotRequest);
    }
}
