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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        calendarService.init();
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

    @Test //test
    void testScheduleWeeklyCalendarRegistration() throws Exception {
        Dentist dentist = Dentist.builder()
                .id("BestDentist")
                .officeName("Flossy Clinic")
                .build();

        List<Dentist> dentistList = new ArrayList<>();
        dentistList.add(dentist);

        Mockito.when(dentistRepository.findAll()).thenReturn(dentistList);

        //simulate MQTT client connection
        Mockito.when(mqttClient.isConnected()).thenReturn(true);

        //simulate successful MQTT publish
        Mockito.doNothing().when(mqttClient).publish(
                Mockito.anyString(),
                Mockito.any(byte[].class),
                Mockito.anyInt(),
                Mockito.anyBoolean()
        );

        calendarService.scheduleWeeklyCalendarRegistration();

        //make sure that publish () is called once
        Mockito.verify(mqttClient, Mockito.times(1)).publish(
                Mockito.anyString(),
                Mockito.any(byte[].class),
                Mockito.anyInt(),
                Mockito.eq(false)
        );
    }
}
