package com.group11.calendarservice.controller;

import com.group11.calendarservice.dto.CalendarRequest;
import com.group11.calendarservice.dto.CalendarResponse;
import com.group11.calendarservice.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/dentist")
@RequiredArgsConstructor
public class CalendarController {
    private final CalendarService calendarService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CalendarResponse createCalendar(@RequestBody CalendarRequest calendarRequest) {
        return calendarService.createCalendar(calendarRequest);
    }
    @PatchMapping("/appointments/{id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public CalendarResponse cancelTimeSlot(@PathVariable String id) throws Exception {
        return calendarService.cancelTimeSlot(id);
    }

    @GetMapping("/test")
    @ResponseStatus(HttpStatus.OK)
    public String testEndpoint() {
        return "Test endpoint is working!";
    }

    @GetMapping("/appointments")
    @ResponseStatus(HttpStatus.OK)
    public List<CalendarResponse> getAllAppointments() {
        return calendarService.getAllAppointments();
    }

    @GetMapping("/appointments/{dentistName}")
    @ResponseStatus(HttpStatus.OK)
    public List<CalendarResponse> getAppointmentsByDentist(@PathVariable String dentistName) {
        return calendarService.getAppointmentsByDentist(dentistName);
    }
}
