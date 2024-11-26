package com.group11.calendarservice.controller;

import com.group11.calendarservice.dto.TimeSlotRequest;
import com.group11.calendarservice.dto.TimeSlotResponse;
import com.group11.calendarservice.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timeslots")
@RequiredArgsConstructor
public class TimeSlotController {

    private final CalendarService calendarService;

    @PostMapping
    public ResponseEntity<?> createTimeSlots(@RequestBody TimeSlotRequest timeSlotRequest) {
        try {
            List<TimeSlotResponse> responses = calendarService.createTimeSlots(timeSlotRequest);
            return new ResponseEntity<>(responses, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}