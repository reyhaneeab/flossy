package com.group11.calendarservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.calendarservice.config.MqttPublishUtil;
import com.group11.calendarservice.dto.CalendarRequest;
import com.group11.calendarservice.dto.CalendarResponse;
import com.group11.calendarservice.dto.TimeSlotRequest;
import com.group11.calendarservice.dto.TimeSlotResponse;
import com.group11.model.Calendar;
import com.group11.model.Dentist;
import com.group11.model.TimeSlot;
import com.group11.calendarservice.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class CalendarService {
    private final DentistRepository dentistRepository;
    private final CalendarRepository calendarRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final MqttClient mqttClient;
    private final MqttPublishUtil mqttPublishUtil;
    private final ObjectMapper objectMapper;

    private static final String CALENDAR_REGISTRATION_TOPIC = "dentist/registration";

    public CalendarService(DentistRepository dentistRepository,
                           CalendarRepository calendarRepository,
                           TimeSlotRepository timeSlotRepository,
                           MqttClient mqttClient,
                           MqttPublishUtil mqttPublishUtil,
                           ObjectMapper objectMapper) {
        this.dentistRepository = dentistRepository;
        this.calendarRepository = calendarRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.mqttClient = mqttClient;
        this.mqttPublishUtil = mqttPublishUtil;
        this.objectMapper = objectMapper;
    }


@PostConstruct
public void init() {
    try {
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.error("MQTT connection lost", cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                log.info("Message arrived on topic {}: {}", topic, new String(message.getPayload()));
                // Handle the message here
                handleMqttMessage(topic, new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                log.info("MQTT delivery complete: {}", token.isComplete());
            }
        });

        if (!mqttClient.isConnected()) {
            mqttClient.connect();
        }

        // Subscribe to the calendar topic
        mqttClient.subscribe("calendar/#", 1);
        log.info("Subscribed to topic: calendar/#");

    } catch (MqttException e) {
        log.error("Error initializing MQTT client", e);
    }
}

private void handleMqttMessage(String topic, String payload) {
    // Implement the logic to handle different calendar-related topics
    if (topic.equals("calendar/view")) {
        // Handle calendar view request
    } else if (topic.equals("calendar/create")) {
        // Handle calendar creation request
    }
    // Add more topic handlers as needed
}
    private TimeSlotResponse mapToResponse(TimeSlot slot) {
        return new TimeSlotResponse(slot.getId(), slot.getDate(), slot.getStartTime(), slot.getEndTime(), slot.getOwner());
    }

    public CalendarResponse createCalendar(String DentistId, CalendarRequest calendarRequest) {
        Dentist dentist = dentistRepository.findById(DentistId)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist office not found"));

        LocalDate current = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());

        List<TimeSlot> timeSlots = calendarRequest.timeSlots().stream().map(slotRequest -> {
            TimeSlot slot = new TimeSlot();
            slot.setStartTime(slotRequest.startTime());
            slot.setEndTime(slotRequest.endTime());
            slot.setDate(slotRequest.date());
            slot.setOwner(dentist);
            return slot;
        }).toList();

        timeSlots = timeSlotRepository.saveAll(timeSlots);

        Calendar calendar = new Calendar();
        calendar.setOwner(dentist);
        calendar.setTimeSlots(timeSlots);
        calendar.setWeekNumber(current.get(weekFields.weekOfWeekBasedYear()));
        calendar.setYear(current.getYear());

        calendarRepository.save(calendar);

        String message = "successfully created calendar";
        mqttPublishUtil.publish(CALENDAR_REGISTRATION_TOPIC, message, 0, true);
        return new CalendarResponse(calendar.getId(), calendar.getOwner().getId(), calendar.getWeekNumber(), calendar.getYear(), calendar.getTimeSlots().stream().map(this::mapToResponse).toList());
    }

    public String handleRequest(String request) {
        try {
            TimeSlotRequest timeSlotRequest = objectMapper.readValue(request, TimeSlotRequest.class);

            // Check for overlapping slots
            List<TimeSlot> overlappingSlots = timeSlotRepository.findOverlappingSlots(
                    timeSlotRequest.startTime(),
                    timeSlotRequest.endTime()
            );

            if (!overlappingSlots.isEmpty()) {
                return "Error: Overlapping time slot detected";
            }

            // Create and save new time slot using the builder pattern
            TimeSlot newTimeSlot = TimeSlot.builder()
                    .startTime(timeSlotRequest.startTime())
                    .endTime(timeSlotRequest.endTime())
                    .date(timeSlotRequest.startTime().toLocalDate())
                    .booked(false)
                    .build();

            TimeSlot savedTimeSlot = timeSlotRepository.save(newTimeSlot);

            return "Time slot created successfully: " + savedTimeSlot.getId();
        } catch (Exception e) {
            return "Error processing request: " + e.getMessage();
        }
    }
}