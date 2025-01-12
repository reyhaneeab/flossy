package com.group11.calendarservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.dto.TimeSlotRequest;
import com.group11.calendarservice.repository.DentistRepository;
import com.group11.model.Dentist;

import org.eclipse.paho.client.mqttv3.MqttClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Captor;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CalendarServiceTest {

    @InjectMocks
    private CalendarService calendarService;
    @Mock
    private DentistRepository dentistRepository;
    @Mock
    private MqttClient mqttClient;
    @Captor
    private ArgumentCaptor<String> topicCaptor;
    @Captor
    private ArgumentCaptor<byte[]> payloadCaptor;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        topicCaptor = ArgumentCaptor.forClass(String.class);
        payloadCaptor = ArgumentCaptor.forClass(byte[].class);
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

        Mockito.when(mqttClient.isConnected()).thenReturn(true);

        Mockito.doNothing().when(mqttClient).publish(
                Mockito.anyString(),
                Mockito.any(byte[].class),
                Mockito.anyInt(),
                Mockito.anyBoolean()
        );

        calendarService.scheduleWeeklyCalendarRegistration();

        //make sure that publish () is called once
        Mockito.verify(mqttClient, Mockito.times(1)).publish(
                topicCaptor.capture(),
                payloadCaptor.capture(),
                Mockito.anyInt(),
                Mockito.anyBoolean()
        );

        //check if topic is "calendar/registration"
        String capturedTopic = topicCaptor.getValue();
        assertTrue(capturedTopic.contains("calendar/registration"),
                "Expected topic to contain 'calendar/registration', but got: " + capturedTopic);

    }
    @Test
    void testScheduleWeeklyCalendarRegistration_NoDentists() throws Exception {
        Mockito.when(dentistRepository.findAll()).thenReturn(new ArrayList<>());

        calendarService.scheduleWeeklyCalendarRegistration();

        //check that publish was never called
        Mockito.verify(mqttClient, Mockito.never()).publish(
                Mockito.anyString(),
                Mockito.any(byte[].class),
                Mockito.anyInt(),
                Mockito.anyBoolean()
        );
    }


}
