package com.group11.calendarservice.service;

import com.group11.calendarservice.config.MqttPublishUtil;
import com.group11.calendarservice.dto.CalendarRequest;
import com.group11.calendarservice.dto.CalendarResponse;
import com.group11.calendarservice.dto.TimeSlotRequest;
import com.group11.calendarservice.dto.TimeSlotResponse;
import com.group11.calendarservice.model.Calendar;
import com.group11.calendarservice.model.TimeSlot;
import com.group11.calendarservice.repository.CalendarRepository;
import com.group11.calendarservice.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import static org.springframework.core.io.support.SpringFactoriesLoader.FailureHandler.handleMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {
    private final CalendarRepository calendarRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final MqttClient mqttClient;
    private final MqttPublishUtil mqttPublishUtil;

    private static final String CALENDAR_REGISTRATION_TOPIC = "dentist/registration";
    private static final String TIMESLOT_TOPIC = "calendar/timeslots";

    @PostConstruct
    public void init() {
        try {
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.error("MQTT connection lost", cause);
                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    log.info("Message arrived: {}", mqttMessage.toString());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    log.info("MQTT delivery complete: {}", token.isComplete());
                }
            });

            if (!mqttClient.isConnected()) {
                mqttClient.connect();
            }
        } catch (MqttException e) {
            log.error("Error initializing MQTT client", e);
        }
    }

    public CalendarResponse createCalendar(CalendarRequest calendarRequest) {
        Calendar calendar = Calendar.builder()
                .date(calendarRequest.date())
                .dentistOffice(calendarRequest.dentistOffice())
                .build();
        calendarRepository.save(calendar);

        String payload = String.format("{\"date\": \"%s\", \"dentistOffice\": \"%s\"}", calendar.getDate(),
                calendar.getDentistOffice());
        mqttPublishUtil.publish(CALENDAR_REGISTRATION_TOPIC, payload, 1, false);

        log.info("Calendar {} has been registered successfully", calendar.getDate());
        return new CalendarResponse(calendar.getDate(), calendar.getDentistOffice());
    }

    public List<TimeSlotResponse> createTimeSlots(TimeSlotRequest timeSlotRequest) {
        return timeSlotRequest.timeSlots().stream().map(this::createTimeSlot).collect(Collectors.toList());
    }

    private TimeSlotResponse createTimeSlot(TimeSlotRequest.TimeSlotDto timeSlotDto) {
        // Check for overlapping time slots
        List<TimeSlot> overlappingSlots = timeSlotRepository.findOverlappingSlots(
                timeSlotDto.dentistName(), timeSlotDto.startTime(), timeSlotDto.endTime());
        if (!overlappingSlots.isEmpty()) {
            throw new IllegalArgumentException("Time slot overlaps with an existing slot for the same doctor");
        }

        // Build and save the TimeSlot object
        LocalDateTime now = LocalDateTime.now();
        TimeSlot timeSlot = TimeSlot.builder()
                .dentistName(timeSlotDto.dentistName())
                .startTime(timeSlotDto.startTime())
                .endTime(timeSlotDto.endTime())
                .dentistOffice(timeSlotDto.dentistOffice())
                .build();
        timeSlotRepository.save(timeSlot);

        // Add the time slot to the calendar
        Calendar calendar = calendarRepository.findByDate(timeSlotDto.startTime().toLocalDate().toString());
        if (calendar == null) {
            calendar = Calendar.builder()
                    .date(timeSlotDto.startTime().toLocalDate().toString())
                    .dentistOffice(timeSlotDto.dentistOffice())
                    .timeSlots(new ArrayList<>())
                    .build();
        }
        if (calendar.getTimeSlots() == null) {
            calendar.setTimeSlots(new ArrayList<>());
        }
        calendar.getTimeSlots().add(timeSlot);
        calendarRepository.save(calendar);

        // Create the payload for MQTT publish
        String payload = String.format(
                "{\"dentistName\": \"%s\", \"startTime\": \"%s\", \"endTime\": \"%s\"}",
                timeSlot.getDentistName(), timeSlot.getStartTime().toString(), timeSlot.getEndTime().toString());

        // Publish the payload using MqttPublishUtil and the TIMESLOT_TOPIC
        try {
            if (!mqttClient.isConnected()) {
                mqttClient.connect();
            }
            mqttPublishUtil.publish(TIMESLOT_TOPIC, payload, 1, false);
            log.info("TimeSlot {} has been published successfully", timeSlot.getId());
        } catch (MqttException e) {
            log.error("Failed to publish message: {}", e.getMessage());
            throw new RuntimeException("Failed to publish message: " + e.getMessage(), e);
        }

        // Log success and return a response object
        log.info("TimeSlot {} has been published successfully", timeSlot.getId());
        return new TimeSlotResponse(
                timeSlot.getId(),
                timeSlot.getDentistName(),
                timeSlot.getStartTime(),
                timeSlot.getEndTime(),
                "TimeSlot has been published successfully");
    }
}