package com.group11.calendarservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.calendarservice.dto.TimeSlotRequest;
import com.group11.calendarservice.repository.DentistRepository;
import com.group11.model.Dentist;

import org.eclipse.paho.client.mqttv3.MqttClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalendarServiceTest {

    @InjectMocks
    private CalendarService calendarService;
    @Mock
    private DentistRepository dentistRepository;
    @Mock
    private MqttClient mqttClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        calendarService = new CalendarService(
                dentistRepository,
                null,
                null,
                mqttClient,
                null,
                new ObjectMapper()
        );

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

    @Test
    void testScheduleWeeklyCalendarRegistration() throws Exception{
        Dentist dentist = Dentist.builder()
                .id("dentist123")
                .officeName("Dental Care")
                .build();

        dentistRepository.findAll().add(dentist);

        calendarService.scheduleWeeklyCalendarRegistration();
    }
}
