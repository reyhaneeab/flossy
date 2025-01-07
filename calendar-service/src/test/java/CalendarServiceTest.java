package com.group11.calendarservice.service;

import com.group11.calendarservice.dto.TimeSlotRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalendarServiceTest {

    @InjectMocks
    private CalendarService calendarService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test //test generateDefaultTimeSlots()
    void testGetDefaultTimeSlotsForWeek() {
        LocalDate startOfWeek = LocalDate.of(2024, 1, 1);  // Monday
        int expectedSlots = 7 * 16;

        List<TimeSlotRequest> timeSlots = calendarService.getDefaultTimeSlotsForWeek(startOfWeek);

        assertEquals(expectedSlots, timeSlots.size());
        assertEquals(LocalDateTime.of(2024, 1, 1, 9, 0), timeSlots.get(0).startTime());
        assertEquals(LocalDateTime.of(2024, 1, 1, 9, 30), timeSlots.get(0).endTime());
    }
}
