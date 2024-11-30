package com.group11.calendarservice.controller;

import com.group11.calendarservice.dto.CalendarRequest;
import com.group11.calendarservice.dto.CalendarResponse;
import com.group11.calendarservice.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {
    private final CalendarService calendarService;

    @PostMapping("/{dentistId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CalendarResponse createCalendar(
            @PathVariable String dentistId,
            @RequestBody CalendarRequest calendarRequest) {
        return calendarService.createCalendar(dentistId, calendarRequest);
    }

}
